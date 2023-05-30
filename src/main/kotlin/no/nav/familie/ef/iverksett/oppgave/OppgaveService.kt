package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseRevurderingInnvilget
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.beskrivelseRevurderingOpphørt
import no.nav.familie.ef.iverksett.oppgave.OppgaveBeskrivelse.tilTekst
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val iverksettingRepository: IverksettingRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun skalOppretteVurderHenvendelseOppgave(iverksett: IverksettOvergangsstønad): Boolean {
        if (iverksett.skalIkkeSendeBrev()) {
            return false
        }

        if (iverksett.behandling.behandlingÅrsak == BehandlingÅrsak.SANKSJON_1_MND) {
            return true
        }
        return when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> true
            BehandlingType.REVURDERING -> {
                when (iverksett.vedtak.vedtaksresultat) {
                    Vedtaksresultat.INNVILGET -> aktivitetEllerPeriodeEndret(iverksett)
                    Vedtaksresultat.OPPHØRT -> true
                    else -> false
                }
            }

            else -> false
        }
    }

    fun opprettOppgaveMedOppfølgingsenhet(iverksett: IverksettOvergangsstønad, oppgaveType: Oppgavetype, beskrivelse: String): Long {
        val enhet = familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(iverksett.søker.personIdent)
            ?: error("Kunne ikke finne enhetsnummer for personident med behandlingsId=${iverksett.behandling.behandlingId}")

        val opprettOppgaveRequest =
            OppgaveUtil.opprettOppgaveRequest(
                eksternFagsakId = iverksett.fagsak.eksternId,
                personIdent = iverksett.søker.personIdent,
                stønadstype = iverksett.fagsak.stønadstype,
                enhetId = enhet.enhetId,
                oppgavetype = oppgaveType,
                beskrivelse = beskrivelse,
                settBehandlesAvApplikasjon = false,
            )

        return oppgaveClient.opprettOppgave(opprettOppgaveRequest)?.let { return it }
            ?: error("Kunne ikke finne oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }

    fun opprettFremleggsoppgave(iverksett: IverksettOvergangsstønad, beskrivelse: String): Long {
        val opprettOppgaveRequest =
            OppgaveUtil.opprettOppgaveRequest(
                eksternFagsakId = iverksett.fagsak.eksternId,
                personIdent = iverksett.søker.personIdent,
                stønadstype = iverksett.fagsak.stønadstype,
                enhetId = iverksett.søker.tilhørendeEnhet,
                oppgavetype = Oppgavetype.Fremlegg,
                beskrivelse = beskrivelse,
                settBehandlesAvApplikasjon = false,
                fristFerdigstillelse = iverksett.vedtak.vedtakstidspunkt.toLocalDate().plusYears(1),
                mappeId = finnMappeForFremleggsoppgave(iverksett.søker.tilhørendeEnhet, iverksett.behandling.behandlingId),
            )

        return oppgaveClient.opprettOppgave(opprettOppgaveRequest)?.let { return it }
            ?: error("Kunne ikke finne oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }

    fun lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett: IverksettOvergangsstønad) =
        when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett)
            BehandlingType.REVURDERING -> finnBeskrivelseForRevurderingAvVedtaksresultat(iverksett)
            else -> error("Kunne ikke finne riktig BehandlingType for oppfølgingsoppgave")
        }

    fun hentOppgave(gsakOppgaveId: Long): Oppgave {
        return oppgaveClient.finnOppgaveMedId(gsakOppgaveId)
    }

    fun oppdaterOppgave(oppgave: Oppgave) {
        oppgaveClient.oppdaterOppgave(oppgave)
    }

    private fun finnMappeForFremleggsoppgave(enhetsnummer: String?, behandlingId: UUID): Long? {
        if (enhetsnummer == "4489" || enhetsnummer == "4483") {
            val mappenavnProd = "41 Revurdering"
            val mappenavnDev = "41 - Revurdering"
            val mapper = finnMapper(enhetsnummer)
            val mappeIdForFremleggsoppgave = mapper.find { it.navn.contains(mappenavnDev) || it.navn.contains(mappenavnProd) }?.id?.toLong()
            mappeIdForFremleggsoppgave?.let {
                logger.info("Legger oppgave i Revurdering vedtak-mappe")
            } ?: run {
                logger.error("Fant ikke mappe for fremleggsoppgave: 41 - Revurdering for enhetsnummer=$enhetsnummer og med behandlingId=$behandlingId")
            }
            return mappeIdForFremleggsoppgave
        }
        return null
    }

    private fun finnMapper(enhet: String): List<MappeDto> {
        val mappeRespons = oppgaveClient.finnMapper(
            enhetsnummer = enhet,
            limit = 1000,
        )
        if (mappeRespons.antallTreffTotalt > mappeRespons.mapper.size) {
            logger.error(
                "Det finnes flere mapper (${mappeRespons.antallTreffTotalt}) " +
                    "enn vi har hentet ut (${mappeRespons.mapper.size}). Sjekk limit. ",
            )
        }
        return mappeRespons.mapper
    }

    private fun finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett: IverksettOvergangsstønad): String {
        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> beskrivelseFørstegangsbehandlingInnvilget(
                iverksett.totalVedtaksperiode(),
                iverksett.gjeldendeVedtak(),
            )

            Vedtaksresultat.AVSLÅTT -> beskrivelseFørstegangsbehandlingAvslått(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
            else -> error("Kunne ikke finne riktig vedtaksresultat for oppfølgingsoppgave")
        }
    }

    private fun finnBeskrivelseForRevurderingAvVedtaksresultat(iverksett: IverksettOvergangsstønad): String {
        if (iverksett.behandling.behandlingÅrsak == BehandlingÅrsak.SANKSJON_1_MND) {
            val sanksjonsvedtakMåned: String = iverksett.finnSanksjonsvedtakMåned().tilTekst()
            return "Bruker har fått vedtak om sanksjon 1 mnd: $sanksjonsvedtakMåned"
        }

        return when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> {
                iverksett.behandling.forrigeBehandlingId?.let {
                    beskrivelseRevurderingInnvilget(
                        iverksett.totalVedtaksperiode(),
                        iverksett.gjeldendeVedtak(),
                    )
                } ?: finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett)
            }

            Vedtaksresultat.OPPHØRT -> beskrivelseRevurderingOpphørt(opphørsdato(iverksett))
            else -> error("Kunne ikke finne riktig vedtaksresultat for oppfølgingsoppgave")
        }
    }

    private fun opphørsdato(iverksett: IverksettOvergangsstønad): LocalDate? {
        val tilkjentYtelse = iverksett.vedtak.tilkjentYtelse ?: error("TilkjentYtelse er null")
        return tilkjentYtelse.andelerTilkjentYtelse.maxOfOrNull { it.periode.tomDato }
    }

    private fun aktivitetEllerPeriodeEndret(iverksett: IverksettOvergangsstønad): Boolean {
        val forrigeBehandlingId = iverksett.behandling.forrigeBehandlingId ?: return true
        val forrigeBehandling = iverksettingRepository.findByIdOrThrow(forrigeBehandlingId).data
        if (forrigeBehandling !is IverksettOvergangsstønad) {
            error("Forrige behandling er av annen type=${forrigeBehandling::class.java.simpleName}")
        }
        if (forrigeBehandling.vedtak.vedtaksresultat == Vedtaksresultat.OPPHØRT) {
            return true
        }
        if (forrigeBehandling.gjeldendeVedtak().periodeType == VedtaksperiodeType.MIGRERING) {
            return false
        }
        return harEndretAktivitet(iverksett, forrigeBehandling) || harEndretPeriode(iverksett, forrigeBehandling)
    }

    private fun harEndretAktivitet(
        iverksett: IverksettOvergangsstønad,
        forrigeBehandling: IverksettOvergangsstønad,
    ): Boolean {
        return iverksett.gjeldendeVedtak().aktivitet != forrigeBehandling.gjeldendeVedtak().aktivitet
    }

    private fun harEndretPeriode(
        iverksett: IverksettOvergangsstønad,
        forrigeBehandling: IverksettOvergangsstønad,
    ): Boolean {
        return iverksett.vedtaksPeriodeMedMaksTilOgMedDato() != forrigeBehandling.vedtaksPeriodeMedMaksTilOgMedDato()
    }

    private fun IverksettOvergangsstønad.gjeldendeVedtak(): VedtaksperiodeOvergangsstønad =
        this.vedtak.vedtaksperioder.maxByOrNull { it.periode } ?: error("Kunne ikke finne vedtaksperioder")

    private fun IverksettOvergangsstønad.vedtaksPeriodeMedMaksTilOgMedDato(): LocalDate {
        return this.vedtak.vedtaksperioder.maxOf { it.periode.tomDato }
    }

    private fun IverksettOvergangsstønad.totalVedtaksperiode(): Pair<LocalDate, LocalDate> =
        Pair(
            this.vedtak.vedtaksperioder.minOf { it.periode.fomDato },
            this.vedtak.vedtaksperioder.maxOf { it.periode.tomDato },
        )

    private fun IverksettOvergangsstønad.finnSanksjonsvedtakMåned(): YearMonth {
        val yearMonth =
            this.vedtak.vedtaksperioder.findLast { it.periodeType == VedtaksperiodeType.SANKSJON }?.periode?.fom
        return yearMonth
            ?: error("Finner ikke periode for iversetting av sanksjon. Behandling: (${this.behandling.behandlingId})")
    }
}
