package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
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
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForOpprettelseType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
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
            BehandlingType.FØRSTEGANGSBEHANDLING -> {
                true
            }

            BehandlingType.REVURDERING -> {
                when (iverksett.vedtak.vedtaksresultat) {
                    Vedtaksresultat.INNVILGET -> aktivitetEllerPeriodeEndret(iverksett)
                    Vedtaksresultat.OPPHØRT -> true
                    else -> false
                }
            }

            else -> {
                false
            }
        }
    }

    fun opprettOppgaveMedOppfølgingsenhet(
        iverksett: IverksettOvergangsstønad,
        oppgaveType: Oppgavetype,
        beskrivelse: String,
    ): Long {
        val enhet =
            familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(iverksett.søker.personIdent)
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

        return oppgaveClient.opprettOppgave(opprettOppgaveRequest) ?: error("Kunne ikke opprette oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }

    fun opprettFremleggsoppgave(
        iverksett: IverksettOvergangsstønad,
        beskrivelse: String,
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
    ): Long {
        val opprettOppgaveRequest =
            OppgaveUtil.opprettOppgaveRequest(
                eksternFagsakId = iverksett.fagsak.eksternId,
                personIdent = iverksett.søker.personIdent,
                stønadstype = iverksett.fagsak.stønadstype,
                enhetId = iverksett.søker.tilhørendeEnhet,
                oppgavetype = Oppgavetype.Fremlegg,
                beskrivelse = beskrivelse,
                settBehandlesAvApplikasjon = false,
                fristFerdigstillelse = lagFristFerdigstillelse(iverksett),
                mappeId = finnMappeForFremleggsoppgave(iverksett.søker.tilhørendeEnhet, iverksett.behandling.behandlingId, oppgaveForOpprettelseType),
                oppgaveForOpprettelseType = oppgaveForOpprettelseType,
            )

        return oppgaveClient.opprettOppgave(opprettOppgaveRequest) ?: error("Kunne ikke opprette oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }

    fun opprettFremleggsoppgaveViaBarnetilsyn(
        iverksett: IverksettBarnetilsyn,
        beskrivelse: String,
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
    ): Long {
        val opprettOppgaveRequest =
            OppgaveUtil.opprettOppgaveRequest(
                eksternFagsakId = iverksett.fagsak.eksternId,
                personIdent = iverksett.søker.personIdent,
                stønadstype = StønadType.OVERGANGSSTØNAD,
                enhetId = iverksett.søker.tilhørendeEnhet,
                oppgavetype = Oppgavetype.Fremlegg,
                beskrivelse = beskrivelse,
                settBehandlesAvApplikasjon = false,
                fristFerdigstillelse = lagFristFerdigstillelseViaBarnetilsyn(iverksett),
                mappeId = finnMappeForFremleggsoppgave(iverksett.søker.tilhørendeEnhet, iverksett.behandling.behandlingId, oppgaveForOpprettelseType),
                oppgaveForOpprettelseType = oppgaveForOpprettelseType,
            )

        return oppgaveClient.opprettOppgave(opprettOppgaveRequest) ?: error("Kunne ikke opprette oppgave for behandlingId=${iverksett.behandling.behandlingId}")
    }

    fun lagFristFerdigstillelse(iverksett: IverksettOvergangsstønad): LocalDate? {
        val femtende = 15

        val erKontrollAvSelvstendig =
            iverksett.vedtak.oppgaverForOpprettelse.oppgavetyper
                .contains(OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE)
        val årForKontrollAvSelvstendig = iverksett.vedtak.oppgaverForOpprettelse.årForInntektskontrollSelvstendigNæringsdrivende

        val fristKontrollAvSelvstendig: LocalDate? =
            if (årForKontrollAvSelvstendig != null && erKontrollAvSelvstendig) {
                LocalDate.of(årForKontrollAvSelvstendig, Month.DECEMBER, femtende)
            } else {
                null
            }

        val vedtaksDato = iverksett.vedtak.vedtakstidspunkt.toLocalDate()

        val fristFerdigstillelse = fristKontrollAvSelvstendig ?: lagFristFerdigstillelseForInntektskontrollEttÅrFrem(vedtaksDato)

        return fristFerdigstillelse
    }

    fun lagFristFerdigstillelseViaBarnetilsyn(iverksett: IverksettBarnetilsyn): LocalDate? {
        val femtende = 15
        val erKontrollAvSelvstendig =
            iverksett.vedtak.oppgaverForOpprettelse.oppgavetyper
                .contains(OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE)
        val årForKontrollAvSelvstendig = iverksett.vedtak.oppgaverForOpprettelse.årForInntektskontrollSelvstendigNæringsdrivende

        val fristKontrollAvSelvstendig: LocalDate? =
            if (årForKontrollAvSelvstendig != null && erKontrollAvSelvstendig) {
                LocalDate.of(årForKontrollAvSelvstendig, Month.DECEMBER, femtende)
            } else {
                null
            }

        val vedtaksdato = iverksett.vedtak.vedtakstidspunkt.toLocalDate()
        val fristFerdigstillelse = fristKontrollAvSelvstendig ?: lagFristFerdigstillelseForInntektskontrollEttÅrFrem(vedtaksdato)

        return fristFerdigstillelse
    }

    fun lagFristFerdigstillelseForInntektskontrollEttÅrFrem(vedtaksdato: LocalDate): LocalDate? {
        // Frist skal ikke falle på
        // - Den 6. dagen i måneden for det er en rutine i enhetene som sier at hvis man ikke får revurdert eller sjekket en sak fordi inntekten for den siste måneden ikke er innrapportert ennå, så oppretter man en fremleggsoppgave med frist den 6. neste måned for å sjekke inntekten. Grunnen til at fristen er den 6. er fordi arbeidsgivers frist til å innrapportere inntekt for forrige måned er den 5.
        // - 17. og 18. mai, for de er forbeholdt innhenting av aktivitetsplikt
        // - Juli og august på grunn av ferie og lav bemanning
        // Det er verdt å merke seg at oppgavesystemet flytter fristen fremover dersom fristdato lander på en helg, så vi må unngå at vi sklir inn i neste mnd

        var ettÅrFremITid = vedtaksdato.plusYears(1)

        if (ettÅrFremITid.monthValue == 7 || ettÅrFremITid.monthValue == 8) ettÅrFremITid = ettÅrFremITid.minusMonths(2)
        if (ettÅrFremITid.monthValue == 9) ettÅrFremITid = ettÅrFremITid.minusMonths(3)
        if (ettÅrFremITid.monthValue == 5 && (ettÅrFremITid.dayOfMonth == 17 || ettÅrFremITid.dayOfMonth == 18)) {
            ettÅrFremITid =
                ettÅrFremITid.minusDays(
                    2,
                )
        }
        if (ettÅrFremITid.dayOfMonth == 6) ettÅrFremITid = ettÅrFremITid.plusDays(1)

        if (erHelgeDag(ettÅrFremITid.dayOfWeek)) {
            if (ettÅrFremITid.dayOfMonth == 1) {
                ettÅrFremITid = ettÅrFremITid.plusDays(2)
            } else {
                ettÅrFremITid = ettÅrFremITid.minusDays(2)
            }
        }
        return ettÅrFremITid
    }

    fun lagOppgavebeskrivelseForVurderHenvendelseOppgave(iverksett: IverksettOvergangsstønad) =
        when (iverksett.behandling.behandlingType) {
            BehandlingType.FØRSTEGANGSBEHANDLING -> finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett)
            BehandlingType.REVURDERING -> finnBeskrivelseForRevurderingAvVedtaksresultat(iverksett)
            else -> error("Kunne ikke finne riktig BehandlingType for oppfølgingsoppgave")
        }

    fun hentOppgave(gsakOppgaveId: Long): Oppgave = oppgaveClient.finnOppgaveMedId(gsakOppgaveId)

    fun oppdaterOppgave(oppgave: Oppgave): Long = oppgaveClient.oppdaterOppgave(oppgave)

    private fun finnMappeForFremleggsoppgave(
        enhetsnummer: String?,
        behandlingId: UUID,
        oppgaveForOpprettelseType: OppgaveForOpprettelseType,
    ): Long? {
        val oppgaveForOpprettelseTypeMappeNavn = mapOppgaveForOpprettelseTypeTilMappeNavn(oppgaveForOpprettelseType)

        if (enhetsnummer == "4489" || enhetsnummer == "4483") {
            val mapper = finnMapper(enhetsnummer)
            val mappeIdForFremleggsoppgave = mapper.find { it.navn.contains(oppgaveForOpprettelseTypeMappeNavn.dev) || it.navn.contains(oppgaveForOpprettelseTypeMappeNavn.prod) }?.id?.toLong()
            mappeIdForFremleggsoppgave?.let {
                logger.info("Legger oppgave i Revurdering vedtak-mappe")
            } ?: run {
                logger.error(
                    "Fant ikke mappe for fremleggsoppgave: 41 - Revurdering for enhetsnummer=$enhetsnummer og med behandlingId=$behandlingId",
                )
            }
            return mappeIdForFremleggsoppgave
        }
        return null
    }

    fun mapOppgaveForOpprettelseTypeTilMappeNavn(oppgaveForOpprettelseType: OppgaveForOpprettelseType): Enhetsmappe =
        when (oppgaveForOpprettelseType) {
            OppgaveForOpprettelseType.INNTEKTSKONTROLL_1_ÅR_FREM_I_TID -> Enhetsmappe.REVURDERING
            OppgaveForOpprettelseType.INNTEKTSKONTROLL_SELVSTENDIG_NÆRINGSDRIVENDE -> Enhetsmappe.SELVSTENDIG_NÆRINGSDRIVENDE
        }

    private fun finnMapper(enhet: String): List<MappeDto> {
        val mappeRespons =
            oppgaveClient.finnMapper(
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

    private fun erHelgeDag(dag: DayOfWeek): Boolean = dag == DayOfWeek.SATURDAY || dag == DayOfWeek.SUNDAY

    private fun finnBeskrivelseForFørstegangsbehandlingAvVedtaksresultat(iverksett: IverksettOvergangsstønad): String =
        when (iverksett.vedtak.vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> {
                beskrivelseFørstegangsbehandlingInnvilget(
                    iverksett.totalVedtaksperiode(),
                    iverksett.gjeldendeVedtak(),
                )
            }

            Vedtaksresultat.AVSLÅTT -> {
                beskrivelseFørstegangsbehandlingAvslått(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
            }

            else -> {
                error("Kunne ikke finne riktig vedtaksresultat for oppfølgingsoppgave")
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

            Vedtaksresultat.OPPHØRT -> {
                beskrivelseRevurderingOpphørt(opphørsdato(iverksett))
            }

            else -> {
                error("Kunne ikke finne riktig vedtaksresultat for oppfølgingsoppgave")
            }
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
    ): Boolean = iverksett.gjeldendeVedtak().aktivitet != forrigeBehandling.gjeldendeVedtak().aktivitet

    private fun harEndretPeriode(
        iverksett: IverksettOvergangsstønad,
        forrigeBehandling: IverksettOvergangsstønad,
    ): Boolean = iverksett.vedtaksPeriodeMedMaksTilOgMedDato() != forrigeBehandling.vedtaksPeriodeMedMaksTilOgMedDato()

    private fun IverksettOvergangsstønad.gjeldendeVedtak(): VedtaksperiodeOvergangsstønad = this.vedtak.vedtaksperioder.maxByOrNull { it.periode } ?: error("Kunne ikke finne vedtaksperioder")

    private fun IverksettOvergangsstønad.vedtaksPeriodeMedMaksTilOgMedDato(): LocalDate = this.vedtak.vedtaksperioder.maxOf { it.periode.tomDato }

    private fun IverksettOvergangsstønad.totalVedtaksperiode(): Pair<LocalDate, LocalDate> =
        Pair(
            this.vedtak.vedtaksperioder.minOf { it.periode.fomDato },
            this.vedtak.vedtaksperioder.maxOf { it.periode.tomDato },
        )

    private fun IverksettOvergangsstønad.finnSanksjonsvedtakMåned(): YearMonth {
        val yearMonth =
            this.vedtak.vedtaksperioder
                .findLast { it.periodeType == VedtaksperiodeType.SANKSJON }
                ?.periode
                ?.fom
        return yearMonth
            ?: error("Finner ikke periode for iversetting av sanksjon. Behandling: (${this.behandling.behandlingId})")
    }
}
