package no.nav.familie.ef.iverksett.arena

import com.ibm.mq.constants.CMQC
import com.ibm.mq.jms.MQConnectionFactory
import com.ibm.mq.jms.MQQueueConnectionFactory
import com.ibm.msg.client.wmq.WMQConstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import javax.jms.ConnectionFactory
import javax.jms.JMSException

@Configuration
@EnableJms
class JmsConfig {

    private val UTF_8_WITH_PUA = 1208

    @Value("\${MQ_QUEUE_MANAGER}")
    lateinit var queueManager: String

    @Value("\${MQ_HOSTNAME}")
    lateinit var hostname: String

    @Value("\${MQ_PORT}")
    lateinit var port: String

    @Value("\${MQ_CHANNEL}")
    lateinit var channel: String

    @Value("\${SERVICEBRUKER}")
    lateinit var servicebruker: String

    @Value("\${SERVICEBRUKER_PASSORD}")
    lateinit var servicebrukerPassord: String


    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate(connectionFactory)
        jmsTemplate.defaultDestinationName = "Q1_475.SOB_VEDTAKHENDELSER_ARE"
        return jmsTemplate
    }

    @Bean
    @Throws(JMSException::class)
    fun queueConnectionFactory(): ConnectionFactory {
        val connectionFactory: MQConnectionFactory = MQQueueConnectionFactory()
        connectionFactory.queueManager = queueManager
        connectionFactory.hostName = hostname
        connectionFactory.port = Integer.valueOf(port)
        connectionFactory.channel = channel
        connectionFactory.transportType = WMQConstants.WMQ_CM_CLIENT
        connectionFactory.ccsid = UTF_8_WITH_PUA
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, CMQC.MQENC_NATIVE)
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA)
        val adapter = UserCredentialsConnectionFactoryAdapter()
        adapter.setTargetConnectionFactory(connectionFactory)
        adapter.setUsername(servicebruker)
        adapter.setPassword(servicebrukerPassord)

        val cachingConnectionFactory = CachingConnectionFactory()
        cachingConnectionFactory.targetConnectionFactory = adapter
        cachingConnectionFactory.sessionCacheSize = 10
        return cachingConnectionFactory
    }
}