package no.nav.familie.ef.iverksett

import no.nav.familie.ef.iverksett.domene.*
import no.nav.familie.ef.iverksett.simulering.SimuleringDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.simulering.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

fun simuleringDto(): SimuleringDto {
    val behandlingId = UUID.randomUUID()
    val tilkjentYtelseMedMetaData = TilkjentYtelseMedMetaData(
        tilkjentYtelse = TilkjentYtelse(
            id = UUID.randomUUID(),
            behandlingId = behandlingId,
            personident = "12345611111",
            stønadFom = LocalDate.of(2021, 1, 1),
            stønadTom = LocalDate.of(2021, 12, 31),
            opphørFom = null,
            utbetalingsoppdrag = null,
            vedtaksdato = LocalDate.of(2021, 5, 1),
            status = TilkjentYtelseStatus.IKKE_KLAR,
            type = TilkjentYtelseType.FØRSTEGANGSBEHANDLING,
            andelerTilkjentYtelse = listOf(
                AndelTilkjentYtelse(
                    periodebeløp = Periodebeløp(
                        utbetaltPerPeriode = 15000,
                        periodetype = Periodetype.MÅNED,
                        fraOgMed = LocalDate.of(2021, 1, 1),
                        tilOgMed = LocalDate.of(2023, 12, 31)
                    ),
                    personIdent = "12345611111",
                    periodeId = 2,
                    forrigePeriodeId = 1,
                    stønadsType = StønadType.OVERGANGSSTØNAD,
                    kildeBehandlingId = UUID.randomUUID()
                )
            )
        ),
        saksbehandlerId = "saksbehandlerId",
        eksternBehandlingId = 1,
        stønadstype = StønadType.OVERGANGSSTØNAD,
        eksternFagsakId = 1
    )

    val tilkjentYtelse = TilkjentYtelse(
        id = UUID.randomUUID(),
        behandlingId = behandlingId,
        personident = "12345611111",
        stønadFom = LocalDate.of(2021, 1, 1),
        stønadTom = LocalDate.of(2023, 12, 31),
        opphørFom = null,
        utbetalingsoppdrag = null,
        vedtaksdato = null,
        status = TilkjentYtelseStatus.IKKE_KLAR,
        type = TilkjentYtelseType.FØRSTEGANGSBEHANDLING,
        andelerTilkjentYtelse = listOf(
            AndelTilkjentYtelse(
                periodebeløp = Periodebeløp(
                    utbetaltPerPeriode = 15000,
                    periodetype = Periodetype.MÅNED,
                    fraOgMed = LocalDate.of(2021, 1, 1),
                    tilOgMed = LocalDate.of(2023, 12, 31)
                ),
                personIdent = "12345611111",
                periodeId = 1,
                forrigePeriodeId = null,
                stønadsType = StønadType.OVERGANGSSTØNAD,
                kildeBehandlingId = UUID.randomUUID()
            )
        )
    )

    return SimuleringDto(tilkjentYtelseMedMetaData, tilkjentYtelse)
}

fun detaljertSimuleringResultat() : DetaljertSimuleringResultat {
    return DetaljertSimuleringResultat(
        simuleringMottaker = listOf(
            SimuleringMottaker(simulertPostering = listOf(
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
            ), mottakerNummer = null, mottakerType = MottakerType.BRUKER)
        )
    )
}