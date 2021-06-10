package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import java.time.LocalDate
import java.util.UUID


fun lagAndelTilkjentYtelse(beløp: Int,
                           periodetype: Periodetype,
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
                            periodetype = periodetype,
                            inntekt = inntekt,
                            samordningsfradrag = samordningsfradrag,
                            inntektsreduksjon = inntektsreduksjon,
                            periodeId = periodeId,
                            forrigePeriodeId = forrigePeriodeId,
                            kildeBehandlingId = kildeBehandlingId)

fun lagAndelTilkjentYtelseDto(beløp: Int,
                              periodetype: Periodetype,
                              fraOgMed: LocalDate,
                              tilOgMed: LocalDate,
                              kildeBehandlingId: UUID? = UUID.randomUUID(),
                              inntekt: Int = 0,
                              samordningsfradrag: Int = 0,
                              inntektsreduksjon: Int = 0) =
        AndelTilkjentYtelseDto(beløp = beløp,
                               fraOgMed = fraOgMed,
                               tilOgMed = tilOgMed,
                               periodetype = periodetype,
                               inntekt = inntekt,
                               samordningsfradrag = samordningsfradrag,
                               inntektsreduksjon = inntektsreduksjon,
                               kildeBehandlingId = kildeBehandlingId)