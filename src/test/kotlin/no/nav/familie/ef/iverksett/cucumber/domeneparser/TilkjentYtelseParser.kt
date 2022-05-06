package no.nav.familie.ef.iverksett.cucumber.domeneparser

import io.cucumber.datatable.DataTable
import no.nav.familie.ef.iverksett.cucumber.domeneparser.IdTIlUUIDHolder.behandlingIdTilUUID
import no.nav.familie.ef.iverksett.cucumber.steps.TilkjentYtelseHolder
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.util.UUID

object TilkjentYtelseParser {

    fun mapStartdatoer(dataTable: DataTable): Map<UUID, LocalDate> {
        return dataTable.asMaps().groupBy {
            it.getValue(Domenebegrep.BEHANDLING_ID.nøkkel)
        }.map { (_, rader) ->
            val rad = rader.single()
            val behandlingId = behandlingIdTilUUID[parseInt(Domenebegrep.BEHANDLING_ID, rad)]!!
            behandlingId to parseDato(TilkjentYtelseDomenebegrep.STARTDATO, rad)
        }.toMap()
    }

    fun mapTilkjentYtelse(dataTable: DataTable,
                          startdatoer: Map<UUID, LocalDate>,
                          stønadstype: StønadType): List<TilkjentYtelseHolder> {
        return dataTable.asMaps().groupBy {
            it.getValue(Domenebegrep.BEHANDLING_ID.nøkkel)
        }.map { (_, rader) ->
            val rad = rader.first()
            val behandlingIdInt = parseInt(Domenebegrep.BEHANDLING_ID, rad)
            val behandlingId = behandlingIdTilUUID[behandlingIdInt]!!
            val andeler = rader.map { mapAndelTilkjentYtelse(it) }
            val startdato = (startdatoer[behandlingId]
                             ?: andeler.minOfOrNull { it.fraOgMed }
                             ?: error("Mangler startdato eller andel for behandling=$behandlingIdInt"))
            TilkjentYtelseHolder(
                    behandlingId = behandlingId,
                    behandlingIdInt = behandlingIdInt,
                    tilkjentYtelse = TilkjentYtelseDto(
                            andelerTilkjentYtelse = andeler,
                            startdato = startdato,
                    ),
                    stønadType = stønadstype
            )
        }
    }

    fun mapForventetUtbetalingsoppdrag(dataTable: DataTable): List<ForventetUtbetalingsoppdrag> {
        return dataTable.asMaps().groupBy {
            it.getValue(Domenebegrep.BEHANDLING_ID.nøkkel)
        }.map { (_, rader) ->
            val rad = rader.first()
            val behandlingId = behandlingIdTilUUID[parseInt(Domenebegrep.BEHANDLING_ID, rad)]!!
            validerAlleKodeEndringerLike(rader)
            ForventetUtbetalingsoppdrag(
                    behandlingId = behandlingId,
                    kodeEndring = parseEnum(UtbetalingsoppdragDomenebegrep.KODE_ENDRING, rad),
                    utbetalingsperiode = rader.map {
                        ForventetUtbetalingsperiode(
                                erEndringPåEksisterendePeriode = parseBoolean(UtbetalingsoppdragDomenebegrep.ER_ENDRING, it),
                                klassifisering = parseString(UtbetalingsoppdragDomenebegrep.KLASSIFISERING, it),
                                periodeId = parseInt(UtbetalingsoppdragDomenebegrep.PERIODE_ID, it).toLong(),
                                forrigePeriodeId = parseInt(UtbetalingsoppdragDomenebegrep.FORRIGE_PERIODE_ID, it).toLong(),
                                sats = parseInt(UtbetalingsoppdragDomenebegrep.BELØP, it),
                                satsType = parseEnum(UtbetalingsoppdragDomenebegrep.TYPE, it),
                                fom = parseDato(Domenebegrep.FRA_DATO, it),
                                tom = parseDato(Domenebegrep.TIL_DATO, it),
                                opphør = parseValgfriDato(UtbetalingsoppdragDomenebegrep.OPPHØRSDATO, it),
                        )
                    }
            )
        }
    }

    private fun validerAlleKodeEndringerLike(rader: List<MutableMap<String, String>>) {
        rader.map { parseEnum<KodeEndring>(UtbetalingsoppdragDomenebegrep.KODE_ENDRING, it) }.zipWithNext().forEach {
            assertThat(it.first).isEqualTo(it.second)
                    .withFailMessage("Alle kodeendringer for en og samme oppdrag må være lik ${it.first} -> ${it.second}")
        }
    }

    private fun mapAndelTilkjentYtelse(rad: MutableMap<String, String>) = AndelTilkjentYtelseDto(
            beløp = parseInt(TilkjentYtelseDomenebegrep.BELØP, rad),
            periodetype = parsePeriodetype(rad) ?: Periodetype.MÅNED,
            inntekt = parseValgfriInt(TilkjentYtelseDomenebegrep.INNTEKT, rad) ?: 0,
            inntektsreduksjon = parseValgfriInt(TilkjentYtelseDomenebegrep.INNTEKTSREDUKSJON, rad) ?: 0,
            samordningsfradrag = parseValgfriInt(TilkjentYtelseDomenebegrep.INNTEKT, rad) ?: 0,
            fraOgMed = parseDato(Domenebegrep.FRA_DATO, rad),
            tilOgMed = parseDato(Domenebegrep.TIL_DATO, rad),
            kildeBehandlingId = null // ikke i bruk i iverksett
    )


    /**
     * Skal vi bare validere et og et oppdrag, sånn att man ikke validerer for mye
     * eks lag en behandling, lag en ny behandling,
     * forvent har expect på forrige behandling eller noe slik
     */

    data class ForventetUtbetalingsoppdrag(
            val behandlingId: UUID,
            val kodeEndring: KodeEndring,
            val utbetalingsperiode: List<ForventetUtbetalingsperiode>
    )

    data class ForventetUtbetalingsperiode(
            val erEndringPåEksisterendePeriode: Boolean,
            val klassifisering: String,
            val periodeId: Long,
            val forrigePeriodeId: Long?,
            val sats: Int,
            val satsType: Utbetalingsperiode.SatsType,
            val fom: LocalDate,
            val tom: LocalDate,
            val opphør: LocalDate?,
    )

    data class ForventetAndelTilkjentYtelse(
            val periodeId: Long,
            val forrigePeriodeId: Long?,
            val fom: LocalDate,
            val tom: LocalDate,
    )

}

enum class TilkjentYtelseDomenebegrep(override val nøkkel: String) : Domenenøkkel {
    STARTDATO("Startdato"),


    INNTEKT("INNTEKT"),
    INNTEKTSREDUKSJON("Inntektsreduksjon"),
    SAMORDNINGSFRADRAG("Samordningsfradrag"),
    BELØP("Beløp"),
    PERIODETYPE("Periodetype"),
    ;

}

enum class UtbetalingsoppdragDomenebegrep(override val nøkkel: String) : Domenenøkkel {
    KODE_ENDRING("Kode endring"),
    ER_ENDRING("Er endring"),
    KLASSIFISERING("Klassifisering"),
    PERIODE_ID("Periode id"),
    FORRIGE_PERIODE_ID("Forrige periode id"),
    BELØP("Beløp"),
    TYPE("Type"),
    OPPHØRSDATO("Opphørsdato")
}
