package no.nav.familie.ef.iverksett

import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.SimuleringDto
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.felles.simulering.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseMedMetadata as TilkjentYtelseMedMetadataDto

fun simuleringDto(): SimuleringDto {
    val behandlingId = UUID.fromString("4b657902-d994-11eb-b8bc-0242ac130003")
    val tilkjentYtelseMedMetaData = TilkjentYtelseMedMetadataDto(
            tilkjentYtelse = TilkjentYtelseDto(
                    andelerTilkjentYtelse = listOf(
                            lagAndelTilkjentYtelseDto(
                                    beløp = 15000,
                                    periodetype = Periodetype.MÅNED,
                                    fraOgMed = LocalDate.of(2021, 1, 1),
                                    tilOgMed = LocalDate.of(2023, 12, 31),
                                    kildeBehandlingId = UUID.randomUUID()
                            )
                    )
            ),
            saksbehandlerId = "saksbehandlerId",
            eksternBehandlingId = 1,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternFagsakId = 1,
            behandlingId = behandlingId,
            personIdent = "12345611111",
            vedtaksdato = LocalDate.of(2021, 5, 1),

            )

    return SimuleringDto(tilkjentYtelseMedMetaData, UUID.randomUUID())
}

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

fun beriketSimuleringsresultat() = BeriketSimuleringsresultat(
        detaljer = detaljertSimuleringResultat(),
        oppsummering = simuleringsoppsummering())

fun simuleringsoppsummering() = Simuleringsoppsummering(
        perioder = listOf(Simuleringsperiode(
                fom = LocalDate.of(2021,1,1),
                tom = LocalDate.of(2021,12,31),
                forfallsdato = LocalDate.of(2021,10,1),
                nyttBeløp = BigDecimal.valueOf(15000),
                tidligereUtbetalt = BigDecimal.ZERO,
                resultat = BigDecimal.valueOf(15000),
                feilutbetaling = BigDecimal.ZERO
        )),
        etterbetaling = BigDecimal.valueOf(15000),
        feilutbetaling = BigDecimal.ZERO,
        fom = LocalDate.of(2021,1,1),
        fomDatoNestePeriode = null,
        tomDatoNestePeriode = null,
        forfallsdatoNestePeriode = null,
        tidSimuleringHentet = LocalDate.now(),
        tomSisteUtbetaling = LocalDate.of(2021,12,31)
)

fun posteringer(fraDato: LocalDate,
                antallMåneder: Int = 1,
                beløp: BigDecimal = BigDecimal(5000),
                posteringstype: PosteringType = PosteringType.YTELSE

): List<SimulertPostering> = MutableList(antallMåneder) { index ->
    SimulertPostering(fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD,
                      fom = fraDato.plusMonths(index.toLong()),
                      tom = fraDato.plusMonths(index.toLong()).with(TemporalAdjusters.lastDayOfMonth()),
                      betalingType = BetalingType.DEBIT,
                      beløp = beløp,
                      posteringType = posteringstype,
                      forfallsdato = fraDato.plusMonths(index.toLong()).with(TemporalAdjusters.lastDayOfMonth()),
                      utenInntrekk = false)
}
