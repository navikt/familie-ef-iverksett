package no.nav.familie.ef.iverksett.felles

import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import no.nav.familie.kontrakter.felles.personopplysning.PersonIdentMedHistorikk
import no.nav.familie.restklient.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class FamilieIntegrasjonerClient(
    @Qualifier("azure") restOperations: RestOperations,
    @Value("\${FAMILIE_INTEGRASJONER_API_URL}")
    private val integrasjonUri: URI,
) : AbstractRestClient(restOperations, "familie.integrasjoner") {
    private val hentIdenterURI =
        UriComponentsBuilder
            .fromUri(integrasjonUri)
            .pathSegment(PATH_HENT_IDENTER)
            .build()
            .toUri()

    private fun arbeidsfordelingOppfølingUri(tema: String) =
        UriComponentsBuilder
            .fromUri(integrasjonUri)
            .pathSegment(PATH_ARBEIDSFORDELING_OPPFØLGING, tema)
            .build()
            .toUri()

    private fun arbeidsfordelingUriMedRelasjoner(tema: String) =
        UriComponentsBuilder
            .fromUri(integrasjonUri)
            .pathSegment(PATH_ARBEIDSFORDELING, tema)
            .pathSegment("med-relasjoner")
            .build()
            .toUri()

    fun hentIdenter(
        personident: String,
        medHistprikk: Boolean,
    ): List<PersonIdentMedHistorikk> {
        val uri =
            UriComponentsBuilder
                .fromUri(hentIdenterURI)
                .queryParam("historikk", medHistprikk)
                .build()
                .toUri()
        val response = postForEntity<Ressurs<FinnPersonidenterResponse>>(uri, PersonIdent(personident))
        return response.getDataOrThrow().identer
    }

    fun hentBehandlendeEnhetForOppfølging(personident: String): Enhet? {
        val response =
            postForEntity<Ressurs<List<Enhet>>>(arbeidsfordelingOppfølingUri(TEMA_ENSLIG_FORSØRGER), Ident(personident))
        return response.getDataOrThrow().firstOrNull()
    }

    fun hentBehandlendeEnhetForBehandlingMedRelasjoner(personident: String): List<Enhet> {
        val response =
            postForEntity<Ressurs<List<Enhet>>>(arbeidsfordelingUriMedRelasjoner(TEMA_ENSLIG_FORSØRGER), Ident(personident))
        return response.getDataOrThrow()
    }

    companion object {
        private const val TEMA_ENSLIG_FORSØRGER = "ENF" // NAY - 4489
        const val PATH_ARBEIDSFORDELING = "api/arbeidsfordeling/enhet"
        const val PATH_ARBEIDSFORDELING_OPPFØLGING = "api/arbeidsfordeling/oppfolging"
        const val PATH_HENT_IDENTER = "api/personopplysning/v1/identer/ENF"
    }
}
