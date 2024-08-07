package no.nav.familie.ef.iverksett.arena

import com.ibm.mq.constants.CMQC
import com.ibm.mq.jakarta.jms.MQConnectionFactory
import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory
import com.ibm.msg.client.jakarta.wmq.WMQConstants
import jakarta.jms.ConnectionFactory
import jakarta.jms.JMSException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate

@Configuration
@EnableJms
@EnableConfigurationProperties(ArenaMqConfigProperties::class)
class ArenaMqConfig(
    val arenaMqConfigProperties: ArenaMqConfigProperties,
) {
    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate(connectionFactory)
        jmsTemplate.defaultDestinationName = arenaMqConfigProperties.queueName
        return jmsTemplate
    }

    @Bean
    @Throws(JMSException::class)
    fun queueConnectionFactory(): ConnectionFactory {
        val connectionFactory: MQConnectionFactory = MQQueueConnectionFactory()
        connectionFactory.queueManager = arenaMqConfigProperties.queueManager
        connectionFactory.hostName = arenaMqConfigProperties.hostName
        connectionFactory.port = Integer.valueOf(arenaMqConfigProperties.port)
        connectionFactory.channel = arenaMqConfigProperties.channel
        connectionFactory.transportType = WMQConstants.WMQ_CM_CLIENT
        connectionFactory.ccsid = UTF_8_WITH_PUA
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, CMQC.MQENC_NATIVE)
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)
        connectionFactory.clientReconnectOptions = WMQConstants.WMQ_CLIENT_RECONNECT // will try to reconnect
        connectionFactory.clientReconnectTimeout = 600 // reconnection attempts for 10 minutes
        val adapter = UserCredentialsConnectionFactoryAdapter()
        adapter.setTargetConnectionFactory(connectionFactory)
        adapter.setUsername(arenaMqConfigProperties.servicebruker)
        adapter.setPassword(arenaMqConfigProperties.servicebrukerPassord)

        val cachingConnectionFactory = CachingConnectionFactory()
        cachingConnectionFactory.targetConnectionFactory = adapter
        cachingConnectionFactory.sessionCacheSize = 10
        return cachingConnectionFactory
    }

    companion object {
        private const val UTF_8_WITH_PUA = 1208
    }
}
