package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.oppdrag.GrensesnittavstemmingRequest
import no.nav.familie.kontrakter.felles.oppdrag.KonsistensavstemmingUtbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.OppdragId
import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class OppdragClient(@Value("\${FAMILIE_OPPDRAG_API_URL}")
                    private val familieOppdragUri: URI,
                    @Qualifier("azure")
                    restOperations: RestOperations) : AbstractPingableRestClient(restOperations, "familie.oppdrag") {

    private val postOppdragUri: URI = UriComponentsBuilder.fromUri(familieOppdragUri).pathSegment("api/oppdrag").build().toUri()

    private val getStatusUri: URI = UriComponentsBuilder.fromUri(familieOppdragUri).pathSegment("api/status").build().toUri()

    private val grensesnittavstemmingUri: URI =
            UriComponentsBuilder.fromUri(familieOppdragUri).pathSegment("api/grensesnittavstemming").build().toUri()

    private val konsistensavstemmingUri: URI =
            UriComponentsBuilder.fromUri(familieOppdragUri).pathSegment("api/konsistensavstemming").build().toUri()

    private val postSimuleringUri: URI =
            UriComponentsBuilder.fromUri(familieOppdragUri).pathSegment("api/simulering/v1").build().toUri()

    fun iverksettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag): String {
        return postForEntity<Ressurs<String>>(postOppdragUri, utbetalingsoppdrag).getDataOrThrow()
    }

    fun hentStatus(oppdragId: OppdragId): OppdragStatusMedMelding {
        val ressurs = postForEntity<Ressurs<OppdragStatus>>(getStatusUri, oppdragId)
        return OppdragStatusMedMelding(ressurs.getDataOrThrow(), ressurs.melding)
    }

    fun grensesnittavstemming(grensesnittavstemmingRequest: GrensesnittavstemmingRequest): String {
        return postForEntity<Ressurs<String>>(grensesnittavstemmingUri, grensesnittavstemmingRequest).getDataOrThrow()
    }

    fun konsistensavstemming(konsistensavstemmingUtbetalingsoppdrag: KonsistensavstemmingUtbetalingsoppdrag): String {
        return postForEntity<Ressurs<String>>(konsistensavstemmingUri, konsistensavstemmingUtbetalingsoppdrag).getDataOrThrow()
    }

    fun hentSimulering(utbetalingsoppdrag: Utbetalingsoppdrag): DetaljertSimuleringResultat {
        return postForEntity<Ressurs<DetaljertSimuleringResultat>>(postSimuleringUri, utbetalingsoppdrag).getDataOrThrow()
    }

    override val pingUri = postOppdragUri

    override fun ping() {
        operations.optionsForAllow(pingUri)
    }
}
