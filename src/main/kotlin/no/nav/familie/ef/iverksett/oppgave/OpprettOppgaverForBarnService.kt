package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Properties

@Service
class OpprettOppgaverForBarnService(private val oppgaveClient: OppgaveClient,
                                    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
                                    private val taskRepository: TaskRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun opprettTaskerForBarn(oppgaverForBarn: List<OppgaveForBarn>) {
        oppgaverForBarn.forEach {
            try {
                taskRepository.save(Task(OpprettOppgaveForBarnTask.TYPE,
                                         objectMapper.writeValueAsString(it),
                                         Properties()))
            } catch (ex: Exception) {
                logger.error("Kunne ikke opprette task for barn som fyller år med OppgaveForBarn=$it")
            }
        }
    }

    fun opprettOppgaveForBarnSomFyllerAar(oppgaveForBarn: OppgaveForBarn) {
        if (innhentDokumentasjonOppgaveFinnes(oppgaveForBarn)) {
            logger.info("Oppgave av type innhent dokumentasjon finnes allerede for behandlingId=${oppgaveForBarn.behandlingId}")
            return
        }
        val oppgaveId = oppgaveClient.opprettOppgave(OppgaveUtil.opprettOppgaveRequest(oppgaveForBarn.eksternFagsakId,
                                                                                       oppgaveForBarn.personIdent,
                                                                                       StønadType.valueOf(oppgaveForBarn.stønadType),
                                                                                       enhetForInnhentDokumentasjon(oppgaveForBarn.personIdent),
                                                                                       Oppgavetype.InnhentDokumentasjon,
                                                                                       oppgaveForBarn.beskrivelse))?.let { it }
                        ?: error("Kunne ikke opprette oppgave for barn med behandlingId=${oppgaveForBarn.behandlingId}")
        logger.info("Opprettet oppgave med oppgaveId=$oppgaveId")
    }

    private fun innhentDokumentasjonOppgaveFinnes(oppgaveForBarn: OppgaveForBarn): Boolean {
        val aktørId = familieIntegrasjonerClient.hentAktørId(oppgaveForBarn.personIdent)
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.ENF,
                                                    aktørId = aktørId,
                                                    oppgavetype = Oppgavetype.InnhentDokumentasjon)
        val oppgaveBeskrivelser = oppgaveClient.hentOppgaver(finnOppgaveRequest)
                ?.mapNotNull { it.beskrivelse }
                ?.map { it.trim().lowercase() }
        return oppgaveBeskrivelser?.contains(oppgaveForBarn.beskrivelse.trim().lowercase()) ?: false
    }

    private fun enhetForInnhentDokumentasjon(personIdent: String): Enhet {
        return familieIntegrasjonerClient.hentBehandlendeEnhetForBehandlingMedRelasjoner(personIdent).first()
    }

}