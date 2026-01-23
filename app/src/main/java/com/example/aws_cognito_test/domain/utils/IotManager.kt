package com.example.aws_cognito_test.domain.utils

import android.content.Context
import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.example.aws_cognito_test.domain.model.Location
import com.google.gson.Gson
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider
import software.amazon.awssdk.crt.io.ClientBootstrap
import software.amazon.awssdk.crt.io.ClientTlsContext
import software.amazon.awssdk.crt.io.TlsContextOptions
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
import java.io.IOException

class IotManager(private val context: Context) {
    private val certificateData = readFile("device.pem.crt")?.trim()
    private val keyData = readFile("private_pkcs8.key")?.trim()
    private val rootCA = readFile("AmazonRootCA1.pem")?.trim()

    private lateinit var client: Mqtt5Client

    fun fetchAndInitIot() {
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoSession = result as AWSCognitoAuthSession
                val identityid = cognitoSession.identityIdResult.value

                if (identityid != null) {
                    initMqttClientWithX()
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
            .withLifeCycleEvents(MqttLifeCycleEvents())

        client = builder.build()
        client.start()

    }

    private fun initMqttClientWithX() {
        val clientEndpoint = readFile("endpoint.txt")?.trim()
        if (clientEndpoint == null || certificateData == null || keyData == null || rootCA == null) {
            Log.e("IotManager", "Missing required credential files in assets!")
            return
        }

        val builder = AwsIotMqtt5ClientBuilder.newDirectMqttBuilderWithMtlsFromMemory(clientEndpoint, certificateData, keyData)
            .withCertificateAuthority(rootCA)
            .withClientId("Rider-1")
            .withSessionExpiryIntervalSeconds(3600L)
            .withSessionBehavior(Mqtt5ClientOptions.ClientSessionBehavior.REJOIN_ALWAYS)
            .withKeepAliveIntervalSeconds(60L)
            .withLifeCycleEvents(MqttLifeCycleEvents())

        client = builder.build()
        client.start()
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
        
        client.publish(publishPacket).whenComplete { _, throwable ->
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

    inner class MqttLifeCycleEvents: Mqtt5ClientOptions.LifecycleEvents {
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
        }
    }
}

