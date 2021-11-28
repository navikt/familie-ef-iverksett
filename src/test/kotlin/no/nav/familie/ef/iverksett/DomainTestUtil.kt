package no.nav.familie.ef.iverksett

import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.familie.ef.iverksett.økonomi.simulering.grupperPosteringerEtterDato
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.BetalingType
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FagOmrådeKode
import no.nav.familie.kontrakter.felles.simulering.MottakerType
import no.nav.familie.kontrakter.felles.simulering.PosteringType
import no.nav.familie.kontrakter.felles.simulering.SimuleringMottaker
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import no.nav.familie.kontrakter.felles.simulering.SimulertPostering
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseMedMetadata as TilkjentYtelseMedMetadataDto

fun simuleringDto(andeler: List<AndelTilkjentYtelseDto> = listOf(lagDefaultAndeler()), forrigeBehandlingId: UUID? = UUID.randomUUID()): SimuleringDto {
    val behandlingId = UUID.fromString("4b657902-d994-11eb-b8bc-0242ac130003")
    val tilkjentYtelseMedMetaData = TilkjentYtelseMedMetadataDto(
            tilkjentYtelse = TilkjentYtelseDto(andelerTilkjentYtelse = andeler),
            saksbehandlerId = "saksbehandlerId",
            eksternBehandlingId = 1,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternFagsakId = 1,
            behandlingId = behandlingId,
            personIdent = "12345611111",
            vedtaksdato = LocalDate.of(2021, 5, 1)
    )

    return SimuleringDto(tilkjentYtelseMedMetaData, forrigeBehandlingId)
}

private fun lagDefaultAndeler() =
        lagAndelTilkjentYtelseDto(
                beløp = 15000,
                periodetype = Periodetype.MÅNED,
                fraOgMed = LocalDate.of(2021, 1, 1),
                tilOgMed = LocalDate.of(2023, 12, 31),
                kildeBehandlingId = UUID.randomUUID()
        )

fun detaljertSimuleringResultat(): DetaljertSimuleringResultat {
    return DetaljertSimuleringResultat(
            simuleringMottaker = listOf(
                    SimuleringMottaker(
                            simulertPostering = listOf(
                                    SimulertPostering(
                                            fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER,
                                            fom = LocalDate.of(2021, 1, 1),
                                            tom = LocalDate.of(2021, 12, 31),
                                            betalingType = BetalingType.DEBIT,
                                            beløp = BigDecimal.valueOf(15000),
                                            posteringType = PosteringType.YTELSE,
                                            forfallsdato = LocalDate.of(2021, 10, 1),
                                            utenInntrekk = false
                                    )
                            ), mottakerNummer = null, mottakerType = MottakerType.BRUKER
                    )
            )
    )
}

fun beriketSimuleringsresultat(feilutbetaling: BigDecimal = BigDecimal.ZERO,
                               fom: LocalDate = LocalDate.of(2021, 1, 1),
                               tom: LocalDate = LocalDate.of(2021, 12, 31)) = BeriketSimuleringsresultat(
        detaljer = detaljertSimuleringResultat(),
        oppsummering = simuleringsoppsummering(feilutbetaling,fom,tom))

fun simuleringsoppsummering(
        feilutbetaling: BigDecimal = BigDecimal.ZERO,
        fom: LocalDate = LocalDate.of(2021, 1, 1),
        tom: LocalDate = LocalDate.of(2021, 12, 31)) =
        Simuleringsoppsummering(
                perioder = listOf(Simuleringsperiode(
                        fom = fom,
                        tom = tom,
                        forfallsdato = LocalDate.of(2021, 10, 1),
                        nyttBeløp = BigDecimal.valueOf(15000),
                        tidligereUtbetalt = BigDecimal.ZERO,
                        resultat = BigDecimal.valueOf(15000),
                        feilutbetaling = feilutbetaling,
                        //etterbetaling = BigDecimal.valueOf(15000)
                )),
                etterbetaling = BigDecimal.valueOf(15000),
                feilutbetaling = feilutbetaling,
                fom = fom,
                fomDatoNestePeriode = null,
                tomDatoNestePeriode = null,
                forfallsdatoNestePeriode = null,
                tidSimuleringHentet = LocalDate.now(),
                tomSisteUtbetaling = tom
        )

fun posteringer(fraDato: YearMonth,
                antallMåneder: Int = 1,
                beløp: Int = 5000,
                posteringstype: PosteringType = PosteringType.YTELSE

): List<SimulertPostering> = MutableList(antallMåneder) { index ->
    SimulertPostering(fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD,
                      fom = fraDato.plusMonths(index.toLong()).atDay(1),
                      tom = fraDato.plusMonths(index.toLong()).atEndOfMonth(),
                      betalingType = BetalingType.DEBIT,
                      beløp = beløp.toBigDecimal(),
                      posteringType = posteringstype,
                      forfallsdato = fraDato.plusMonths(index.toLong()).atEndOfMonth(),
                      utenInntrekk = false)
}

fun Tilbakekrevingsdetaljer.medFeilutbetaling(feilutbetaling: BigDecimal, periode: Periode) =
        this.copy(tilbakekrevingMedVarsel =
                  this.tilbakekrevingMedVarsel?.copy(
                          sumFeilutbetaling = feilutbetaling,
                          perioder = listOf(periode)
                  )
        )

fun Int.januar(år: Int) = LocalDate.of(år, 1, this)
fun Int.august(år: Int) = LocalDate.of(år, 8, this)
fun januar(år: Int) = YearMonth.of(år, 1)
fun februar(år: Int) = YearMonth.of(år, 2)
fun juli(år: Int) = YearMonth.of(år, 7)

fun List<SimulertPostering>.tilSimuleringsperioder() =
        grupperPosteringerEtterDato(this.tilSimuleringMottakere())

fun List<SimulertPostering>.tilSimuleringMottakere() =
        listOf(SimuleringMottaker(this, "12345678901", MottakerType.BRUKER))

fun List<SimulertPostering>.tilDetaljertSimuleringsresultat() =
        DetaljertSimuleringResultat(this.tilSimuleringMottakere())
