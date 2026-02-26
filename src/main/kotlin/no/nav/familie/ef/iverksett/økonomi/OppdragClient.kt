package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsgeneratorHelper.KonsistensavstemmingUtbetalingsoppdrag
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.restklient.client.AbstractPingableRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

@Service
class OppdragClient(
    @Value("\${FAMILIE_OPPDRAG_API_URL}")
    private val familieOppdragUri: URI,
    @Qualifier("azure")
    restOperations: RestOperations,
) : AbstractPingableRestClient(restOperations, "familie.oppdrag") {
    private val postOppdragUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragUri)
            .pathSegment("api/oppdrag")
            .build()
            .toUri()

    private val getStatusUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragUri)
            .pathSegment("api/status")
            .build()
            .toUri()

    private val grensesnittavstemmingUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragUri)
            .pathSegment("api/grensesnittavstemming")
            .build()
            .toUri()

    private val konsistensavstemmingUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragUri)
            .pathSegment("api/konsistensavstemming")
            .build()
            .toUri()

    private val postSimuleringUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragUri)
            .pathSegment("api/simulering/v1")
            .build()
            .toUri()

    private val timeoutTestUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragUri)
            .pathSegment("api/timeout-test")
            .build()
            .toUri()

    fun iverksettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag): String = postForEntity<Ressurs<String>>(postOppdragUri, utbetalingsoppdrag).getDataOrThrow()

    fun hentStatus(oppdragId: OppdragId): OppdragStatusMedMelding {
        val ressurs = postForEntity<Ressurs<OppdragStatus>>(getStatusUri, oppdragId)
        return OppdragStatusMedMelding(ressurs.getDataOrThrow(), ressurs.melding)
    }

    fun grensesnittavstemming(grensesnittavstemmingRequest: GrensesnittavstemmingRequest): String = postForEntity<Ressurs<String>>(grensesnittavstemmingUri, grensesnittavstemmingRequest).getDataOrThrow()

    fun konsistensavstemming(
        konsistensavstemmingUtbetalingsoppdrag: KonsistensavstemmingUtbetalingsoppdrag,
        sendStartmelding: Boolean = true,
        sendAvsluttmelding: Boolean = true,
        transaksjonId: UUID? = null,
    ): String {
        val url =
            UriComponentsBuilder
                .fromUri(konsistensavstemmingUri)
                .queryParam("sendStartmelding", sendStartmelding)
                .queryParam("sendAvsluttmelding", sendAvsluttmelding)
                .queryParam("transaksjonId", transaksjonId.toString())
                .build()
                .toUri()
        return postForEntity<Ressurs<String>>(url, konsistensavstemmingUtbetalingsoppdrag).getDataOrThrow()
    }

    fun testTimeout(
        antallSekunderTimeout: Long,
    ): String {
        val uri =
            UriComponentsBuilder
                .fromUri(timeoutTestUri)
                .queryParam("sekunder", antallSekunderTimeout)
                .build()
                .toUri()
        return getForEntity<Ressurs<String>>(uri).getDataOrThrow()
    }

    fun hentSimuleringsresultat(utbetalingsoppdrag: Utbetalingsoppdrag): DetaljertSimuleringResultat = postForEntity<Ressurs<DetaljertSimuleringResultat>>(postSimuleringUri, utbetalingsoppdrag).getDataOrThrow()

    override val pingUri = postOppdragUri

    override fun ping() {
        operations.optionsForAllow(pingUri)
    }
}
