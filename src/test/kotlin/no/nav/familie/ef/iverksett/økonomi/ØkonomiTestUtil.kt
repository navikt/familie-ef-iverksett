package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import java.time.LocalDate
import java.util.UUID


fun lagAndelTilkjentYtelse(beløp: Int,
                           fraOgMed: LocalDate,
                           tilOgMed: LocalDate,
                           periodeId: Long? = null,
                           forrigePeriodeId: Long? = null,
                           kildeBehandlingId: UUID? = UUID.randomUUID(),
                           inntekt: Int = 0,
                           samordningsfradrag: Int = 0,
                           inntektsreduksjon: Int = 0) =
        AndelTilkjentYtelse(beløp = beløp,
                            fraOgMed = fraOgMed,
                            tilOgMed = tilOgMed,
                            inntekt = inntekt,
                            samordningsfradrag = samordningsfradrag,
                            inntektsreduksjon = inntektsreduksjon,
                            periodeId = periodeId,
                            forrigePeriodeId = forrigePeriodeId,
                            kildeBehandlingId = kildeBehandlingId)

fun lagAndelTilkjentYtelseDto(beløp: Int,
                              fraOgMed: LocalDate = LocalDate.of(2021, 1, 1),
                              tilOgMed: LocalDate = LocalDate.of(2021, 1, 31),
                              kildeBehandlingId: UUID = UUID.randomUUID(),
                              inntekt: Int = 0,
                              samordningsfradrag: Int = 0,
                              inntektsreduksjon: Int = 0) =
        AndelTilkjentYtelseDto(beløp = beløp,
                               fraOgMed = fraOgMed,
                               tilOgMed = tilOgMed,
                               inntekt = inntekt,
                               samordningsfradrag = samordningsfradrag,
                               inntektsreduksjon = inntektsreduksjon,
                               kildeBehandlingId = kildeBehandlingId)