package no.nav.familie.ef.iverksett.arena

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import no.nav.melding.virksomhet.vedtakhendelser.v1.vedtakhendelser.WSVedtakHendelser
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
class VedtakhendelseProducer(val jmsTemplate: JmsTemplate) {


    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun produce(vedtakHendelse: WSVedtakHendelser) {

        logger.info("Sender melding på MQ-kø")
        val vedtakHendelseXml = XmlMapper().writeValueAsString(vedtakHendelse)
        jmsTemplate.convertAndSend(vedtakHendelseXml)
        logger.info("Melding sendt på MQ-kø")
    }

}