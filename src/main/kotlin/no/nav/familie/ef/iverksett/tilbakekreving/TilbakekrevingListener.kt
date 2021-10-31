package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandlingRequest
import no.nav.familie.log.mdc.MDCConstants
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TilbakekrevingListener(
        private val iverksettingRepository: IverksettingRepository,
        private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
        private val tilbakekrevingProducer: TilbakekrevingProducer
) : ConsumerSeekAware {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(id = "familie-ef-iverksett",
                   topics = ["teamfamilie.privat-tbk-hentfagsystemsbehandling-request-topic"],
                   containerFactory = "concurrentTilbakekrevingListenerContainerFactory")
    fun listen(consumerRecord: ConsumerRecord<String, String>) {
        secureLogger.info("HentFagsystemsbehandlingRequest er mottatt: $consumerRecord")
        val key: String = consumerRecord.key()
        val data: String = consumerRecord.value()
        try {
            MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString())
            val request: HentFagsystemsbehandlingRequest =
                    objectMapper.readValue(data, HentFagsystemsbehandlingRequest::class.java)
            val iverksett = iverksettingRepository.hentAvEksternId(request.eksternId.toLong())
            val enhet: Enhet = familieIntegrasjonerClient.hentBehandlendeEnhet(iverksett.søker.personIdent)!!
            val fagsystemsbehandling = iverksett.tilFagsystembehandling(enhet)
            tilbakekrevingProducer.send(fagsystemsbehandling)
        } catch (e: Exception) {
            logger.error("Feil ved håndtering av HentFagsystemsbehandlingRequest: ${key}")
            secureLogger.error("Feil ved håndtering av HentFagsystemsbehandlingRequest: ${consumerRecord}")
            throw e
        } finally {
            MDC.remove(MDCConstants.MDC_CALL_ID)
        }
    }

}