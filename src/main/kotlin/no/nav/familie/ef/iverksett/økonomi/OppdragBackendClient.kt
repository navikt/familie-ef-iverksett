package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class OppdragBackendClient(
    @Value("\${FAMILIE_OPPDRAG_BACKEND_API_URL}")
    private val familieOppdragBackendUri: URI,
    @Qualifier("oppdragBackendRestClient")
    private val restClient: RestClient,
) {
    private val postSimuleringUri: URI =
        UriComponentsBuilder
            .fromUri(familieOppdragBackendUri)
            .pathSegment("api/simulering/v1")
            .build()
            .toUri()

    fun hentSimuleringsresultat(utbetalingsoppdrag: Utbetalingsoppdrag): DetaljertSimuleringResultat =
        restClient
            .post()
            .uri(postSimuleringUri)
            .body(utbetalingsoppdrag)
            .retrieve()
            .body<Ressurs<DetaljertSimuleringResultat>>()!!
            .getDataOrThrow()
}
