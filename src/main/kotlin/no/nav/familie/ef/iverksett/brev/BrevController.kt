package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.brev.domain.FrittståendeBrev
import no.nav.familie.ef.iverksett.brev.frittstående.FrittståendeBrevRepository
import no.nav.familie.ef.iverksett.brev.frittstående.JournalførFrittståendeBrevTask
//import no.nav.familie.ef.iverksett.brev.frittstående.JournalførFrittståendeBrevTask
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevDto
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker as BrevmottakerKontrakter

@RestController
@RequestMapping(path = ["/api/brev"])
@ProtectedWithClaims(issuer = "azuread")
class BrevController(
    private val frittståendeBrevRepository: FrittståendeBrevRepository,
    private val taskService: TaskService
) {

    @PostMapping("/frittstaende")
    fun distribuerFrittståendeBrev(
        @RequestBody data: FrittståendeBrevDto,
        mottakere: List<BrevmottakerKontrakter> // TODO flytt til dto
    ): ResponseEntity<Any> {
        // TODO legg tillbake forrige kode her for å støtte at man ikke ennå sender inn liste på mottakere?
        opprettTask(data, mottakere)

        return ResponseEntity.ok().build()
    }

    private fun opprettTask(
        data: FrittståendeBrevDto,
        mottakere: List<no.nav.familie.kontrakter.ef.iverksett.Brevmottaker>
    ) {
        val brev = frittståendeBrevRepository.insert(
            FrittståendeBrev(
                personIdent = data.personIdent,
                eksternFagsakId = data.eksternFagsakId,
                journalførendeEnhet = data.journalførendeEnhet,
                saksbehandlerIdent = data.saksbehandlerIdent,
                stønadstype = data.stønadType,
                mottakere = mottakere.toDomain(),
                fil = data.fil,
                brevtype = data.brevtype
            )
        )
        taskService.save(Task(JournalførFrittståendeBrevTask.TYPE, brev.id.toString()))
    }
}
