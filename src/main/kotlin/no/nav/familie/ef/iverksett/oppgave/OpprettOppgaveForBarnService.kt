package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OpprettOppgaveForBarnService(private val oppgaveClient: OppgaveClient,
                                   private val iverksettingRepository: IverksettingRepository,
                                   private val familieIntegrasjonerClient: FamilieIntegrasjonerClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun opprettOppgaveForBarnSomFyllerAar(oppgaveForBarn: OppgaveForBarn) {
        val behandling = iverksettingRepository.hent(oppgaveForBarn.id)
        if (innhentDokumentasjonOppgaveFinnes(behandling, oppgaveForBarn)) {
            logger.info("Oppgave av type innhent dokumentasjon finnes allerede for behandlingId=${oppgaveForBarn.id}")
            return
        }
        val oppgaveId = oppgaveClient.opprettOppgave(OppgaveUtil.opprettOppgaveRequest(behandling,
                                                                                       enhetForInnhentDokumentasjon(behandling.søker.personIdent),
                                                                                       Oppgavetype.InnhentDokumentasjon,
                                                                                       oppgaveForBarn.beskrivelse))?.let { it }
                        ?: error("Kunne ikke opprette oppgave for barn med behandlingId=${oppgaveForBarn.id}")
        logger.info("Opprettet oppgave med oppgaveId=$oppgaveId")
    }

    private fun innhentDokumentasjonOppgaveFinnes(iverksett: Iverksett, oppgaveForBarn: OppgaveForBarn): Boolean {
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.ENF,
                                                    aktørId = iverksett.søker.personIdent,
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