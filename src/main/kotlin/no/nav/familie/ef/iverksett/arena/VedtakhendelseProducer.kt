package no.nav.familie.ef.iverksett.arena

import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Component
class VedtakhendelseProducer(val jmsTemplate: JmsTemplate) {


    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun produce(message: String) {

        logger.info("Sender melding på MQ-kø")
        jmsTemplate.convertAndSend(message)
        logger.info("Melding sendt på MQ-kø")
    }

}