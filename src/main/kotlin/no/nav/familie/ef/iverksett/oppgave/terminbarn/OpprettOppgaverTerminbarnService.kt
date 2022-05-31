package no.nav.familie.ef.iverksett.oppgave.terminbarn

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.oppgave.OppgaveClient
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForBarn
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OpprettOppgaverTerminbarnService(
    private val oppgaveClient: OppgaveClient,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val taskRepository: TaskRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun opprettTaskerForTerminbarn(oppgaverForBarn: List<OppgaveForBarn>) {
        taskRepository.saveAll(
            oppgaverForBarn.map {
                Task(OpprettOppgaverTerminbarnTask.TYPE, ObjectMapperProvider.objectMapper.writeValueAsString(it))
            }
        )
    }

    fun opprettOppgaveForTerminbarn(oppgaveForBarn: OppgaveForBarn) {

        val opprettOppgaveRequest = OppgaveUtil.opprettOppgaveRequest(
            oppgaveForBarn.eksternFagsakId,
            oppgaveForBarn.personIdent,
            oppgaveForBarn.stønadType,
            enhetForInnhentDokumentasjon(oppgaveForBarn.personIdent),
            Oppgavetype.InnhentDokumentasjon,
            oppgaveForBarn.beskrivelse,
            oppgaveForBarn.aktivFra
        )
        val oppgaveId = oppgaveClient.opprettOppgave(opprettOppgaveRequest)
            ?: error("Kunne ikke opprette oppgave for barn med behandlingId=${oppgaveForBarn.behandlingId}")
        logger.info("Opprettet oppgave med oppgaveId=$oppgaveId for behandling=${oppgaveForBarn.behandlingId}")
    }

    private fun enhetForInnhentDokumentasjon(personIdent: String): Enhet {
        return familieIntegrasjonerClient.hentBehandlendeEnhetForBehandlingMedRelasjoner(personIdent).first()
    }
}
