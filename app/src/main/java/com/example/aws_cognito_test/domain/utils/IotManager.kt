package com.example.aws_cognito_test.domain.utils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.io.IOException
import android.util.Log
import android.content.Context
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions
import software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn
import software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn
import software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn
import software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn
import software.amazon.awssdk.crt.mqtt5.OnStoppedReturn
import software.amazon.awssdk.crt.mqtt5.PublishReturn
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider
import software.amazon.awssdk.crt.io.ClientBootstrap
import software.amazon.awssdk.crt.io.TlsContextOptions
import software.amazon.awssdk.crt.io.ClientTlsContext
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import software.amazon.awssdk.crt.mqtt.QualityOfService
import software.amazon.awssdk.crt.mqtt5.QOS

class IotManager(private val context: Context) {
    // private val clientEndpoint = readFile("endpoint.txt")
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
            .withPublishEvents(MqttPublishEvents())

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
            .withLifeCycleEvents(MqttLifeCycleEvents())
            .withPublishEvents(MqttPublishEvents())
        

        client = builder.build()
        start()
    }

    private fun start() {
        client.start()
        publishTestMessage()
    }

    private fun publishTestMessage() {
        Log.d("IotManager", "Sending a test message...")
        val topic = "android"
        val jsonPayload = """{"message": "Hello from Android!", "timestamp": "${System.currentTimeMillis()}"}"""

        val publishPacket = PublishPacket.PublishPacketBuilder()
            .withTopic(topic)
            .withPayload(jsonPayload.toByteArray())
            .withQOS(QOS.AT_LEAST_ONCE)
            .build()
        
        client.publish(publishPacket).whenComplete { res, throwable ->
            if (throwable != null) {
                Log.e("IotManager", "Publish failed: ${throwable.message}")
            } else {
                Log.d("IotManager", "Message published successfully to $topic")
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

        override fun onConnectionSuccess(
            client: Mqtt5Client?,
            onConnectionSuccessReturn: OnConnectionSuccessReturn?
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

    inner class MqttPublishEvents: Mqtt5ClientOptions.PublishEvents {
        override fun onMessageReceived(
            client: Mqtt5Client?,
            publishReturn: PublishReturn?
        ) {
            Log.d("IotManager", "Message received!")
        }

    }
}

