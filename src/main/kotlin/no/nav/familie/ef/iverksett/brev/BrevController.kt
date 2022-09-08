package no.nav.familie.ef.iverksett.brev

import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/brev"])
@ProtectedWithClaims(issuer = "azuread")
class BrevController(
    private val journalpostClient: JournalpostClient
) {

    @PostMapping("/frittstaende")
    fun distribuerFrittståendeBrev(@RequestBody data: FrittståendeBrevDto): ResponseEntity<Any> {
        val journalpostId = journalpostClient.arkiverDokument(
            ArkiverDokumentRequest(
                fnr = data.personIdent,
                forsøkFerdigstill = true,
                hoveddokumentvarianter = listOf(
                    Dokument(
                        data.fil,
                        Filtype.PDFA,
                        dokumenttype = stønadstypeTilDokumenttype(data.stønadType),
                        tittel = data.brevtype.tittel
                    )
                ),
                fagsakId = data.eksternFagsakId.toString(),
                journalførendeEnhet = data.journalførendeEnhet
            ),
            data.saksbehandlerIdent
        ).journalpostId

        journalpostClient.distribuerBrev(journalpostId, Distribusjonstype.VIKTIG)
        return ResponseEntity.ok().build()
    }
}
