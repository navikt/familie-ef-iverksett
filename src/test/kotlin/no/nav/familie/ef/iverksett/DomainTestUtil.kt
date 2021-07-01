package no.nav.familie.ef.iverksett

import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.simulering.SimuleringDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.felles.simulering.BetalingType
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FagOmrådeKode
import no.nav.familie.kontrakter.felles.simulering.MottakerType
import no.nav.familie.kontrakter.felles.simulering.PosteringType
import no.nav.familie.kontrakter.felles.simulering.SimuleringMottaker
import no.nav.familie.kontrakter.felles.simulering.SimulertPostering
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

fun simuleringDto(): SimuleringDto {
    val behandlingId = UUID.fromString("4b657902-d994-11eb-b8bc-0242ac130003")
    val tilkjentYtelseMedMetaData = TilkjentYtelseMedMetaData(
            tilkjentYtelse = TilkjentYtelse(
                    id = UUID.randomUUID(),
                    utbetalingsoppdrag = null,
                    status = TilkjentYtelseStatus.IKKE_KLAR,
                    andelerTilkjentYtelse = listOf(
                            lagAndelTilkjentYtelse(
                                    beløp = 15000,
                                    periodetype = Periodetype.MÅNED,
                                    fraOgMed = LocalDate.of(2021, 1, 1),
                                    tilOgMed = LocalDate.of(2023, 12, 31),
                                    periodeId = 2,
                                    forrigePeriodeId = 1,
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