package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.felles.Månedsperiode
import java.util.UUID

fun lagAndelTilkjentYtelse(
    beløp: Int,
    periode: Månedsperiode,
    periodeId: Long? = null,
    forrigePeriodeId: Long? = null,
    kildeBehandlingId: UUID? = UUID.randomUUID(),
    inntekt: Int = 0,
    samordningsfradrag: Int = 0,
    inntektsreduksjon: Int = 0
) =
    AndelTilkjentYtelse(
        beløp = beløp,
        periode = periode,
        inntekt = inntekt,
        samordningsfradrag = samordningsfradrag,
        inntektsreduksjon = inntektsreduksjon,
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
        kildeBehandlingId = kildeBehandlingId
    )

fun lagAndelTilkjentYtelseDto(
    beløp: Int,
    periode: Månedsperiode = Månedsperiode("2021-01" to "2021-01"),
    kildeBehandlingId: UUID = UUID.randomUUID(),
    inntekt: Int = 0,
    samordningsfradrag: Int = 0,
    inntektsreduksjon: Int = 0
) =
    AndelTilkjentYtelseDto(
        beløp = beløp,
        periode = periode,
        fraOgMed = periode.fomDato,
        tilOgMed = periode.tomDato,
        inntekt = inntekt,
        samordningsfradrag = samordningsfradrag,
        inntektsreduksjon = inntektsreduksjon,
        kildeBehandlingId = kildeBehandlingId
    )
