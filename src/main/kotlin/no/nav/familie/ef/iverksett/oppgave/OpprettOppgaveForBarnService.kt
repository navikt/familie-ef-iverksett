package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.søknad.Fødselsnummer
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OpprettOppgaveForBarnService(private val oppgaveClient: OppgaveClient,
                                   private val iverksettingRepository: IverksettingRepository) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun opprettOppgaverForAlleBarnSomFyller(innenAntallUker: Long) {
        val behandlinger = iverksettingRepository.hentAlleBehandlinger()
        behandlinger.forEach { iverksett ->
            iverksett.søker.barn.forEach { barn ->
                barn.personIdent?.let {
                    kanOppretteOppgaveForBarnSomFyllerÅr(it)?.let {
                        if (barnBlirEttÅr(innenAntallUker, it)) {
                            opprettOppgaveForBarn(iverksett, OppgaveBeskrivelse.beskrivelseBarnFyllerEttÅr())
                            secureLogger.info("Opprettet innhentDokumentasjon-oppgave for barn med personident=$it")
                        } else if (barnBlirSeksMnd(innenAntallUker, it)) {
                            opprettOppgaveForBarn(iverksett, OppgaveBeskrivelse.beskrivelseBarnBlirSeksMnd())
                            secureLogger.info("Opprettet innhentDokumentasjons-oppgave for barn med personident=$it")
                        }
                    }
                }
            }
        }
    }

    private fun kanOppretteOppgaveForBarnSomFyllerÅr(fødselsnummer: String): Fødselsnummer? {
        return try {
            Fødselsnummer(fødselsnummer)
        } catch (ex: IllegalArgumentException) {
            secureLogger.warn("Kunne ikke opprette Fødselsnummer objekt av barn med fødselsnummer=$fødselsnummer")
            null
        }
    }

    private fun opprettOppgaveForBarn(iverksett: Iverksett, beskrivelse: String): Long {
        val opprettOppgaveRequest = OppgaveUtil.opprettOppgaveRequest(iverksett, Oppgavetype.InnhentDokumentasjon, beskrivelse)
        return oppgaveClient.opprettOppgave(opprettOppgaveRequest)?.let { it }
               ?: error("Kunne ikke opprette oppgave for barn med behandlingId=${iverksett.behandling.behandlingId}")
    }

    private fun barnBlirEttÅr(innenAntallUker: Long, fødselsnummer: Fødselsnummer): Boolean {
        return barnErUnder(12L, fødselsnummer)
               && LocalDate.now().plusWeeks(innenAntallUker).isAfterOrEqual(fødselsnummer.fødselsdato.plusYears(1))
    }

    private fun barnBlirSeksMnd(innenAntallUker: Long, fødselsnummer: Fødselsnummer): Boolean {
        return barnErUnder(6L, fødselsnummer)
               && LocalDate.now().plusWeeks(innenAntallUker).isAfterOrEqual(fødselsnummer.fødselsdato.plusMonths(6L))
    }

    private fun barnErUnder(antallMnd: Long, fødselsnummer: Fødselsnummer): Boolean {
        return LocalDate.now().isBefore(fødselsnummer.fødselsdato.plusMonths(antallMnd))
    }

    private fun LocalDate.isAfterOrEqual(date : LocalDate) : Boolean {
        return this.isEqual(date) || this.isAfter(date)
    }

}