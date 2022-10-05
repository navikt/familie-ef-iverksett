package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevRepository
import no.nav.familie.ef.iverksett.brev.frittstående.JournalførFrittståendeBrevTask
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
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
    private val frittståendeBrevRepository: FrittståendeBrevRepository,
    private val taskService: TaskService,
    private val journalpostClient: JournalpostClient
) {

    @PostMapping("/frittstaende")
    fun distribuerFrittståendeBrev(
        @RequestBody data: FrittståendeBrevDto,
    ): ResponseEntity<Any> {

        if (data.mottakere == null) {
            journalførOgDistribuerBrev(data)
        } else {
            opprettTask(data)
        }

        return ResponseEntity.ok().build()
    }

    private fun journalførOgDistribuerBrev(data: FrittståendeBrevDto) {
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
    }

    private fun opprettTask(data: FrittståendeBrevDto) {
        val mottakere = data.mottakere
        if(mottakere == null || mottakere.isEmpty()) {
            throw IllegalArgumentException("Liste med brevmottakere kan ikke være tom")
        }

        val brev = frittståendeBrevRepository.insert(
            FrittståendeBrev(
                personIdent = data.personIdent,
                eksternFagsakId = data.eksternFagsakId,
                journalførendeEnhet = data.journalførendeEnhet,
                saksbehandlerIdent = data.saksbehandlerIdent,
                stønadstype = data.stønadType,
                mottakere = Brevmottakere(mottakere.map { it.toDomain() }),
                fil = data.fil,
                brevtype = data.brevtype
            )
        )
        taskService.save(Task(JournalførFrittståendeBrevTask.TYPE, brev.id.toString()))
    }
}
