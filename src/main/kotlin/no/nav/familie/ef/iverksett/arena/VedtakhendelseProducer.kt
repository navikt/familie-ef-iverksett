package no.nav.familie.ef.iverksett.arena

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat


@Component
class VedtakhendelseProducer(val jmsTemplate: JmsTemplate) {


    private val logger = LoggerFactory.getLogger(javaClass)

    private val xmlMapper = XmlMapper()
        .registerModule(JavaTimeModule())
        .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"))


    @Transactional
    fun produce(vedtakHendelse: VedtakHendelser) {
        logger.info("Sender melding på MQ-kø")
        val vedtakHendelseXml = xmlMapper.writeValueAsString(vedtakHendelse)
        jmsTemplate.convertAndSend(vedtakHendelseXml)
        logger.info("Melding sendt på MQ-kø")
    }

}