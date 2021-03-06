package no.nav.familie.ef.iverksett.oppgave.barnsalder

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.oppgave.OppgaveClient
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForBarn
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class OpprettOppgaverForBarnService(
    private val oppgaveClient: OppgaveClient,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val taskRepository: TaskRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    private val EF_ENHETNUMMER = "4489"

    @Transactional
    fun opprettTaskerForBarn(oppgaverForBarn: List<OppgaveForBarn>) {
        oppgaverForBarn.forEach {
            try {
                taskRepository.save(
                    Task(
                        OpprettOppgaveForBarnTask.TYPE,
                        objectMapper.writeValueAsString(it)
                    )
                )
            } catch (ex: Exception) {
                secureLogger.error("Kunne ikke opprette task for barn som fyller år med OppgaveForBarn=$it")
                throw ex
            }
        }
    }

    fun opprettOppgaveForBarnSomFyllerAar(oppgaveForBarn: OppgaveForBarn) {
        if (oppgaveFinnesAllerede(oppgaveForBarn)) {
            logger.info("Oppgave av type innhent dokumentasjon finnes allerede for behandlingId=${oppgaveForBarn.behandlingId}")
            return
        }

        val oppgaveType = if (oppgaveForBarn.aktivFra == null) Oppgavetype.InnhentDokumentasjon else Oppgavetype.Fremlegg

        val opprettOppgaveRequest = OppgaveUtil.opprettOppgaveRequest(
            oppgaveForBarn.eksternFagsakId,
            oppgaveForBarn.personIdent,
            oppgaveForBarn.stønadType,
            enhetForInnhentDokumentasjon(oppgaveForBarn.personIdent),
            oppgaveType,
            oppgaveForBarn.beskrivelse,
            oppgaveForBarn.aktivFra
        )
        val oppgaveId = oppgaveClient.opprettOppgave(opprettOppgaveRequest)
            ?: error("Kunne ikke opprette oppgave for barn med behandlingId=${oppgaveForBarn.behandlingId}")
        logger.info("Opprettet oppgave med oppgaveId=$oppgaveId for behandling=${oppgaveForBarn.behandlingId}")

        try {
            leggOppgaveIMappe(oppgaveId)
        } catch (e: Exception) {
            logger.error("Feil under knytning av mappe til oppgave - se securelogs for stacktrace")
            secureLogger.error("Feil under knytning av mappe til oppgave", e)
        }
    }

    private fun oppgaveFinnesAllerede(oppgaveForBarn: OppgaveForBarn): Boolean {
        val aktørId = familieIntegrasjonerClient.hentAktørId(oppgaveForBarn.personIdent)
        val fristdato = oppgaveForBarn.aktivFra ?: LocalDate.now()
        val finnOppgaveRequest = FinnOppgaveRequest(
            tema = Tema.ENF,
            aktørId = aktørId,
            fristFomDato = fristdato.minusWeeks(2),
            fristTomDato = fristdato.plusWeeks(2)
        )
        val oppgaveBeskrivelser = oppgaveClient.hentOppgaver(finnOppgaveRequest)
            .mapNotNull { it.beskrivelse }
            .map { it.trim().lowercase() }
        return oppgaveBeskrivelser.contains(oppgaveForBarn.beskrivelse.trim().lowercase())
    }

    private fun enhetForInnhentDokumentasjon(personIdent: String): Enhet {
        return familieIntegrasjonerClient.hentBehandlendeEnhetForBehandlingMedRelasjoner(personIdent).first()
    }

    fun leggOppgaveIMappe(oppgaveId: Long) {
        val oppgave = oppgaveClient.finnOppgaveMedId(oppgaveId)
        if (oppgave.tildeltEnhetsnr == EF_ENHETNUMMER) { // Skjermede personer skal ikke puttes i mappe
            val finnMappeRequest = FinnMappeRequest(
                listOf(),
                oppgave.tildeltEnhetsnr ?: error("Fikk ikke tildelt enhetsnummer for oppgave med id: $oppgaveId"),
                null,
                1000
            )
            val mapperResponse = oppgaveClient.finnMapper(finnMappeRequest)
            val mappe = mapperResponse.mapper.find {
                it.navn.contains("EF Sak", true) &&
                    it.navn.contains("Hendelser") &&
                    it.navn.contains("62")
            }
                ?: error("Fant ikke mappe for hendelser")
            oppgaveClient.oppdaterOppgave(oppgave.copy(mappeId = mappe.id.toLong()))
        }
    }
}
