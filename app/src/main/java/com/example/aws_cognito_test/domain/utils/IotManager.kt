package com.example.aws_cognito_test.domain.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.example.aws_cognito_test.BuildConfig
import com.example.aws_cognito_test.data.utils.CryptoUtils
import com.example.aws_cognito_test.domain.model.Certificates
import com.example.aws_cognito_test.domain.model.IotIdentity
import com.example.aws_cognito_test.domain.model.Location
import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.domain.usecase.FetchCertificatesUseCase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider
import software.amazon.awssdk.crt.io.ClientBootstrap
import software.amazon.awssdk.crt.io.ClientTlsContext
import software.amazon.awssdk.crt.io.TlsContextOptions
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.crt.mqtt.QualityOfService
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions
import software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn
import software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn
import software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn
import software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn
import software.amazon.awssdk.crt.mqtt5.OnStoppedReturn
import software.amazon.awssdk.crt.mqtt5.QOS
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder
import software.amazon.awssdk.iot.iotidentity.IotIdentityClient
import software.amazon.awssdk.iot.iotidentity.model.CreateKeysAndCertificateRequest
import software.amazon.awssdk.iot.iotidentity.model.CreateKeysAndCertificateSubscriptionRequest
import software.amazon.awssdk.iot.iotidentity.model.RegisterThingRequest
import software.amazon.awssdk.iot.iotidentity.model.RegisterThingSubscriptionRequest
import java.io.IOException
import java.security.KeyStore

const val KEY_ALIAS = "rider-private-key"

class IotManager(
    private val context: Context,
    private val fetchCertificatesUseCase: FetchCertificatesUseCase,
    private val repository: AuthRepository
) {
    private val certificateData = readFile("device.pem.crt")?.trim()
    private val keyData = readFile("private_pkcs8.key")?.trim()
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var client: Mqtt5Client? = null
    private var iotIdentity: IotIdentity? = null
    private var jwt: String? = null

    private var isTransitioningToPermanent = false

    fun fetchAndInitIot(token: String) {
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoSession = result as AWSCognitoAuthSession
                val identityId = cognitoSession.identityIdResult.value

                if (identityId != null) {
                    initMqttClientWithCustom(token) 
                } else {
                    Log.e("IotManager", "User is not signed in!")
                }
            },
            { error ->
                Log.d("IotManager", "Failed to fetch session: ${error.message}")
            }
        )
    }
    
    private fun initMqttClientWithCognito(identityId: String) {
        val clientEndpoint = "cognito-identity.ap-southeast-1.amazonaws.com"
        val websocketConfig = AwsIotMqtt5ClientBuilder.WebsocketSigv4Config()
        val cognitoBuilder = CognitoCredentialsProvider.CognitoCredentialsProviderBuilder()
            .withEndpoint(clientEndpoint)
            .withIdentity(identityId)
            .withClientBootstrap(ClientBootstrap.getOrCreateStaticDefault())

        val cognitoTlsContextOptions = TlsContextOptions.createDefaultClient()
        val cognitoTlsContext = ClientTlsContext(cognitoTlsContextOptions)
        cognitoTlsContextOptions.close()
        cognitoBuilder.withTlsContext(cognitoTlsContext)
        websocketConfig.credentialsProvider = cognitoBuilder.build()
        
        val builder = AwsIotMqtt5ClientBuilder.newWebsocketMqttBuilderWithSigv4Auth(clientEndpoint, websocketConfig)
            .withLifeCycleEvents(MqttLifeCycleEvents {})

        client = builder.build()
        client?.start()

    }

    private suspend fun forceCrash(): Throwable {
        Log.d("IotManager", "Forcing crash...")
        repository.clearIdentityAndKeys()
        throw RuntimeException()
    }

    suspend fun fetchAndInitWithCerts(token: String) {
        val savedIdentity = repository.fetchIotIdentity()
        jwt = token

        if (savedIdentity != null) {
            Log.d("IotManager", "Found stored credentials")
            connectWithPermanentIdentity(token)
        } else {
            initMqttClientWithX("Rider-123", token)
        }
    }

    private suspend fun initMqttClientWithX(riderId: String, token: String) {
        val certificates = fetchCertificatesUseCase(riderId, token) // get temporary certificates
        val clientEndpoint = BuildConfig.AWS_IOT_ENDPOINT
        val rootCA = readFile("AmazonRootCA1.pem")?.trim()

        val builder = AwsIotMqtt5ClientBuilder.newDirectMqttBuilderWithMtlsFromMemory(clientEndpoint, certificates.certificatePem, certificates.privateKey)
            .withCertificateAuthority(rootCA)
            .withClientId(riderId)
            .withSessionExpiryIntervalSeconds(3600L)
            .withSessionBehavior(Mqtt5ClientOptions.ClientSessionBehavior.REJOIN_ALWAYS)
            .withKeepAliveIntervalSeconds(60L)
            .withLifeCycleEvents(MqttLifeCycleEvents {
                ioScope.launch {
                    repository.clearIdentityAndKeys()
                    fetchAndInitWithCerts(token)
                }
            })

        client = builder.build().apply {
            start()

            val connection = MqttClientConnection(this, null)
            providePermanentIdentity(riderId, connection) // profile.name
        }
    }

    private suspend fun providePermanentIdentity(riderId: String, connection: MqttClientConnection) {
        Log.d("IotManager", "Providing permanent credentials...")
        val identityClient = IotIdentityClient(connection)

        identityClient.SubscribeToCreateKeysAndCertificateAccepted(
            CreateKeysAndCertificateSubscriptionRequest(), // Request to get permanent credentials
            QualityOfService.AT_LEAST_ONCE
        ) { keysResponse ->
            Log.d("IotManager", "Received permanent credentials!")
            val permanentCert = keysResponse.certificatePem // permanent device cert
            val permanentPrivateKey = keysResponse.privateKey // permanent private key
            val token = keysResponse.certificateOwnershipToken

            ioScope.launch {
                try {
                    iotIdentity = IotIdentity(
                        riderId, 
                        permanentCert,
                        null,
                        null
                    )

                    repository.saveIotIdentity(iotIdentity!!, permanentPrivateKey)
                    repository.savePrivateKeyToKeystore(KEY_ALIAS, permanentPrivateKey, permanentCert)

                    val registerRequest = RegisterThingRequest().apply {
                        templateName = "RiderAppTemplate"
                        certificateOwnershipToken = token
                        parameters = hashMapOf("SerialNumber" to riderId)
                    }

                    identityClient.PublishRegisterThing(registerRequest, QualityOfService.AT_LEAST_ONCE)
                    Log.d("IotManager", "Register request sent for $riderId")

                } catch (e: Exception) {
                    Log.e("IotManager", "Failed to save or publish identity: ${e.message}")
                }

            }
        }

        identityClient.SubscribeToRegisterThingAccepted(
            RegisterThingSubscriptionRequest().apply { templateName = "RiderAppTemplate" },
            QualityOfService.AT_LEAST_ONCE
        ) { response ->
            Log.d("IotManager", "Success! Permanent Thing created: ${response.thingName}")
            ioScope.launch {
                if (iotIdentity?.certPem != null) {
                    isTransitioningToPermanent = true
                    client?.stop()
                } else {
                    Log.e("IotManager", "Registration accepted but keys not stored yet!")
                }
            }
        }

        identityClient.PublishCreateKeysAndCertificate(
            CreateKeysAndCertificateRequest(),
            QualityOfService.AT_LEAST_ONCE
        )
    }

    private suspend fun connectWithPermanentIdentity(token: String) {
        Log.d("IotManager", "Connecting with permanent credentials...")
        val clientEndpoint = BuildConfig.AWS_IOT_ENDPOINT
        val identity = repository.fetchIotIdentity()

        if (identity?.iv != null && identity.encryptedKey != null) {
            val privateKeyPem = CryptoUtils.decrypt(
                iv = Base64.decode(identity.iv, Base64.DEFAULT),
                encryptedData = Base64.decode(identity.encryptedKey, Base64.DEFAULT),
            )

            try {
                val builder = AwsIotMqtt5ClientBuilder.newDirectMqttBuilderWithMtlsFromMemory(
                    clientEndpoint,
                    identity.certPem,
                    privateKeyPem
                )

                builder.withClientId(identity.thingName)
                    .withSessionExpiryIntervalSeconds(3600L)
                    .withSessionBehavior(Mqtt5ClientOptions.ClientSessionBehavior.REJOIN_ALWAYS)
                    .withKeepAliveIntervalSeconds(60L)
                    .withLifeCycleEvents(MqttLifeCycleEvents {
                        ioScope.launch {
                            client?.stop()
                            repository.clearIdentityAndKeys()
                            fetchAndInitWithCerts(token)
                        }
                    })

                client = builder.build()
                client?.start()
                Log.d("IotManager", "Successfully connected with permanent credentials!")
            } catch(e: Exception) {
                Log.e("IotManager", "Failed to connect permanent: ${e.message}")
            }
        } else {
            Log.e("IotManager", "No IV and EncryptedKey")
        }
    }

    fun initMqttClientWithCustom(token: String) {
        Log.d("IoTManager", "Token: $token")
        val clientEndpoint = BuildConfig.AWS_IOT_ENDPOINT
        val customAuthConfig = AwsIotMqtt5ClientBuilder.MqttConnectCustomAuthConfig().apply {
            authorizerName = "CustomAuthorizer"
            password = token.toByteArray(Charsets.UTF_8)
            username = "guest"
            tokenKeyName = null
            tokenValue = null
            tokenSignature = null
        }
        val builder = AwsIotMqtt5ClientBuilder.newWebsocketMqttBuilderWithCustomAuth(clientEndpoint, customAuthConfig)
        .withLifeCycleEvents(MqttLifeCycleEvents {})

        try {
            client = builder.build()
            Log.d("IoTManager", "Successfully built MQTT Client!")
            client?.start()
        } catch (e: Exception) {
            Log.e("IoTManager", "Error building client: ${e.message}")
        }
    }

    fun publishMessage(deviceId: String, jobOrderId: String, location: Location) {
        val topic = "tracker/$deviceId/update"
        val jsonTree = Gson().toJsonTree(location, Location::class.java)
        val jsonObject = jsonTree.asJsonObject
        jsonObject.addProperty("deviceId", deviceId)
        jsonObject.addProperty("jobOrderId", jobOrderId)
        val jsonPayload = jsonObject.toString()

        val publishPacket = PublishPacket.PublishPacketBuilder()
            .withTopic(topic)
            .withPayload(jsonPayload.toByteArray())
            .withQOS(QOS.AT_LEAST_ONCE)
            .build()
        
        client?.publish(publishPacket)?.whenComplete { _, throwable ->
            if (throwable != null) {
                Log.e("IotManager", "Publish failed: ${throwable.message}")
            } else {
                Log.d("IotManager", "Published $location\nto $topic")
            }
        }
    }

    private fun readFile(fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("IotManager", "Error reading file: ${e.message}")
            null
        }
    }

    inner class MqttLifeCycleEvents(
        private val onAuthError: () -> Unit
    ): Mqtt5ClientOptions.LifecycleEvents {
        override fun onAttemptingConnect(
            client: Mqtt5Client?,
            onAttemptingConnectReturn: OnAttemptingConnectReturn?
        ) {
            Log.d("IotManager", "Attempting to connect...")
        }
        override fun onConnectionSuccess( client: Mqtt5Client?, onConnectionSuccessReturn: OnConnectionSuccessReturn?
        ) {
            Log.d("IotManager", "Connection success!")
        }

        override fun onConnectionFailure(
            client: Mqtt5Client?,
            onConnectionFailureReturn: OnConnectionFailureReturn?
        ) {
            Log.d("IotManager", "Connection failed: ${onConnectionFailureReturn?.errorCode}")
            if (onConnectionFailureReturn?.errorCode == 5134) {
                Log.d("IotManager", "Calling onAuthError")
                onAuthError()
            }
        }

        override fun onDisconnection(
            client: Mqtt5Client?,
            onDisconnectionReturn: OnDisconnectionReturn?
        ) {
            Log.d("IotManager", "Disconnected!")
        }

        override fun onStopped(
            client: Mqtt5Client?,
            onStoppedReturn: OnStoppedReturn?
        ) {
            Log.d("IotManager", "Stopped!")
            ioScope.launch {
                if (isTransitioningToPermanent) {
                    jwt?.let {
                        connectWithPermanentIdentity(it)
                    }
                } else {
                    Log.e("IotManager", "Failed to fetch device certificate!")
                }
            }
        }
    }
}

