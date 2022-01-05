package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.søknad.Fødselsnummer
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class OpprettOppgaveForBarnService(private val oppgaveClient: OppgaveClient,
                                   private val iverksettingRepository: IverksettingRepository) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")
    private val logger = LoggerFactory.getLogger(javaClass)

    fun opprettOppgaverForAlleBarnSomFyller(innenAntallUker: Long, sisteKjøring: LocalDate) {
        val referanseDato = referanseDato(innenAntallUker, sisteKjøring)
        val behandlinger = iverksettingRepository.hentAlleBehandlinger()
        behandlinger.forEach { iverksett ->
            iverksett.søker.barn.forEach { barn ->
                barn.personIdent?.let {
                    opprettFødselsnummer(it)?.let {
                        if (barnBlirEttÅr(innenAntallUker, referanseDato, it)) {
                            opprettOppgaveForBarn(iverksett, OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr())
                            secureLogger.info("Opprettet innhentDokumentasjon-oppgave for barn med behandlingId=${iverksett.behandling.behandlingId}")
                        } else if (barnBlirSeksMnd(innenAntallUker, referanseDato, it)) {
                            opprettOppgaveForBarn(iverksett, OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd())
                            secureLogger.info("Opprettet innhentDokumentasjons-oppgave for barn med behandlingId=${iverksett.behandling.behandlingId}")
                        }
                    }
                }
            }
        }
    }

    private fun opprettFødselsnummer(fødselsnummer: String): Fødselsnummer? {
        return try {
            Fødselsnummer(fødselsnummer)
        } catch (ex: IllegalArgumentException) {
            secureLogger.warn("Kunne ikke opprette Fødselsnummer objekt av barn med fødselsnummer=$fødselsnummer")
            null
        }
    }

    private fun opprettOppgaveForBarn(iverksett: Iverksett, beskrivelse: String) {
        if (innhentDokumentasjonOppgaveFinnes(iverksett)) {
            logger.info("Oppgave av type innhent dokumentasjon finnes allerede")
            return
        }
        val oppgaveId = oppgaveClient.opprettOppgave(OppgaveUtil.opprettOppgaveRequest(iverksett,
                                                                                       enhetForInnhentDokumentasjon(iverksett),
                                                                                       Oppgavetype.InnhentDokumentasjon,
                                                                                       beskrivelse))?.let { it }
                        ?: error("Kunne ikke opprette oppgave for barn med behandlingId=${iverksett.behandling.behandlingId}")
        logger.info("Opprettet oppgave med oppgaveId=$oppgaveId")
    }

    private fun innhentDokumentasjonOppgaveFinnes(iverksett: Iverksett): Boolean {
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.ENF, aktørId = iverksett.søker.personIdent)
        val oppgaveTyper = oppgaveClient.hentOppgaver(finnOppgaveRequest)?.map { it.oppgavetype }
        return oppgaveTyper?.contains(Oppgavetype.InnhentDokumentasjon.name) ?: false
    }

    private fun barnBlirEttÅr(innenAntallUker: Long, referanseDato: LocalDate, fødselsnummer: Fødselsnummer): Boolean {
        return barnErUnder(12L, referanseDato, fødselsnummer)
               && LocalDate.now().plusWeeks(innenAntallUker) >= fødselsnummer.fødselsdato.plusYears(1)
    }

    private fun barnBlirSeksMnd(innenAntallUker: Long, referanseDato: LocalDate, fødselsnummer: Fødselsnummer): Boolean {
        return barnErUnder(6L, referanseDato, fødselsnummer)
               && LocalDate.now().plusWeeks(innenAntallUker) >= fødselsnummer.fødselsdato.plusMonths(6L)
    }

    private fun barnErUnder(antallMnd: Long, referanseDato: LocalDate, fødselsnummer: Fødselsnummer): Boolean {
        return referanseDato <= fødselsnummer.fødselsdato.plusMonths(antallMnd)
    }

    private fun referanseDato(innenAntallUker: Long, sisteKjøring: LocalDate): LocalDate {
        val periodeGap = ChronoUnit.DAYS.between(sisteKjøring, LocalDate.now()) - innenAntallUker * 7
        if (periodeGap > 0) {
            return LocalDate.now().minusDays(periodeGap)
        }
        return LocalDate.now()
    }

    private fun enhetForInnhentDokumentasjon(iverksett: Iverksett): Enhet {

        /** TODO : Legg til sjekk for egenansatte (4483 hvis egenansatt) */

        return Enhet("4489", "")
    }

}