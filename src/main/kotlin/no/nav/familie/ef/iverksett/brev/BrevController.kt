package no.nav.familie.ef.iverksett.brev

import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/api/brev"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class BrevController(
    val journalpostClient: JournalpostClient
) {

    @PostMapping("/frittstaaendebrev")
    fun distribuerFrittståendeBrev(@RequestBody data: FrittståendeBrevDto): ResponseEntity<Any> {
        val journalpostId = journalpostClient.arkiverDokument(
            ArkiverDokumentRequest(
                fnr = data.personIdent,
                forsøkFerdigstill = true,
                //Tittel og filnavn blir satt av integrasjoner
                hoveddokumentvarianter = listOf(Dokument(data.fil, Filtype.PDFA, dokumenttype = Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV, tittel = data.brevtype.tittel)),
                fagsakId = data.eksternFagsakId.toString(),
                journalførendeEnhet = data.journalførendeEnhet,
            ),
            "beslutterId/saksbehandler"
        ).journalpostId

        journalpostClient.distribuerBrev(journalpostId)
        return ResponseEntity.ok().build()
    }
}