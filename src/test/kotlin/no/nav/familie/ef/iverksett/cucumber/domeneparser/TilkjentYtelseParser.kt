package no.nav.familie.ef.iverksett.cucumber.domeneparser

import io.cucumber.datatable.DataTable
import no.nav.familie.ef.iverksett.cucumber.domeneparser.IdTIlUUIDHolder.behandlingIdTilUUID
import no.nav.familie.ef.iverksett.cucumber.steps.TilkjentYtelseHolder
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag.KodeEndring
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode.SatsType
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.util.UUID

object TilkjentYtelseParser {

    fun mapStartdatoer(dataTable: DataTable): Map<UUID, LocalDate> {
        return dataTable.groupByBehandlingId().map { (_, rader) ->
            val rad = rader.single()
            val behandlingId = behandlingIdTilUUID[parseInt(Domenebegrep.BEHANDLING_ID, rad)]!!
            behandlingId to parseÅrMåned(TilkjentYtelseDomenebegrep.STARTDATO, rad).atDay(1)
        }.toMap()
    }

    fun mapTilkjentYtelse(
        dataTable: DataTable,
        startdatoer: Map<UUID, LocalDate>,
        medAndel: Boolean = true
    ): List<TilkjentYtelseHolder> {
        return dataTable.groupByBehandlingId().map { (_, rader) ->
            val rad = rader.first()
            val behandlingIdInt = parseInt(Domenebegrep.BEHANDLING_ID, rad)
            val behandlingId = behandlingIdTilUUID[behandlingIdInt]!!
            val andeler = if (medAndel) {
                rader.map { mapAndelTilkjentYtelse(it) }
            } else {
                listOf()
            }
            val startdato = (
                startdatoer[behandlingId]
                    ?: andeler.minOfOrNull { it.fraOgMed }
                    ?: error("Mangler startdato eller andel for behandling=$behandlingIdInt")
                )
            TilkjentYtelseHolder(
                behandlingId = behandlingId,
                behandlingIdInt = behandlingIdInt,
                tilkjentYtelse = TilkjentYtelseDto(
                    andelerTilkjentYtelse = andeler,
                    startdato = startdato
                )
            )
        }
    }

    fun mapForventetUtbetalingsoppdrag(
        dataTable: DataTable,
        medUtbetalingsperiode: Boolean = true
    ): List<ForventetUtbetalingsoppdrag> {
        return dataTable.groupByBehandlingId().map { (_, rader) ->
            val rad = rader.first()
            val behandlingId = behandlingIdTilUUID[parseInt(Domenebegrep.BEHANDLING_ID, rad)]!!
            validerAlleKodeEndringerLike(rader)
            ForventetUtbetalingsoppdrag(
                behandlingId = behandlingId,
                kodeEndring = parseEnum(UtbetalingsoppdragDomenebegrep.KODE_ENDRING, rad),
                utbetalingsperiode = if (medUtbetalingsperiode) rader.map { mapForventetUtbetalingsperiode(it) } else listOf()
            )
        }
    }

    private fun mapForventetUtbetalingsperiode(it: MutableMap<String, String>) =
        ForventetUtbetalingsperiode(
            erEndringPåEksisterendePeriode = parseBoolean(UtbetalingsoppdragDomenebegrep.ER_ENDRING, it),
            periodeId = parseInt(UtbetalingsoppdragDomenebegrep.PERIODE_ID, it).toLong(),
            forrigePeriodeId = parseValgfriInt(UtbetalingsoppdragDomenebegrep.FORRIGE_PERIODE_ID, it)?.toLong(),
            sats = parseInt(UtbetalingsoppdragDomenebegrep.BELØP, it),
            satsType = parseValgfriEnum<SatsType>(UtbetalingsoppdragDomenebegrep.TYPE, it) ?: SatsType.MND,
            fom = parseÅrMåned(Domenebegrep.FRA_DATO, it).atDay(1),
            tom = parseÅrMåned(Domenebegrep.TIL_DATO, it).atEndOfMonth(),
            opphør = parseValgfriÅrMåned(UtbetalingsoppdragDomenebegrep.OPPHØRSDATO, it)?.atDay(1)
        )

    fun mapForventetTilkjentYtelse(dataTable: DataTable, behandlingIdInt: Int, startdato: LocalDate?): ForventetTilkjentYtelse {
        val andeler = dataTable.asMaps().map { mapForventetAndel(it) }
        return ForventetTilkjentYtelse(
            behandlingId = behandlingIdTilUUID[behandlingIdInt]!!,
            andeler = andeler,
            startdato = startdato ?: andeler.minOfOrNull { it.fom }
                ?: error("Mangler startdato når det ikke finnes noen andeler for behandling=$behandlingIdInt")
        )
    }

    private fun mapForventetAndel(rad: MutableMap<String, String>) = ForventetAndelTilkjentYtelse(
        fom = parseValgfriÅrMåned(Domenebegrep.FRA_DATO, rad)?.atDay(1) ?: LocalDate.MIN,
        tom = parseValgfriÅrMåned(Domenebegrep.TIL_DATO, rad)?.atEndOfMonth() ?: LocalDate.MIN,
        beløp = parseInt(TilkjentYtelseDomenebegrep.BELØP, rad),
        periodeId = parseInt(TilkjentYtelseDomenebegrep.PERIODE_ID, rad).toLong(),
        forrigePeriodeId = parseValgfriInt(TilkjentYtelseDomenebegrep.FORRIGE_PERIODE_ID, rad)?.toLong(),
        kildeBehandlingId = behandlingIdTilUUID[parseValgfriInt(TilkjentYtelseDomenebegrep.KILDE_BEHANDLING_ID, rad)]
    )

    private fun DataTable.groupByBehandlingId() =
        this.asMaps().groupBy {
            it.getValue(Domenebegrep.BEHANDLING_ID.nøkkel)
        }

    private fun validerAlleKodeEndringerLike(rader: List<MutableMap<String, String>>) {
        rader.map { parseEnum<KodeEndring>(UtbetalingsoppdragDomenebegrep.KODE_ENDRING, it) }.zipWithNext().forEach {
            assertThat(it.first).isEqualTo(it.second)
                .withFailMessage("Alle kodeendringer for en og samme oppdrag må være lik ${it.first} -> ${it.second}")
        }
    }

    private fun mapAndelTilkjentYtelse(rad: MutableMap<String, String>) = AndelTilkjentYtelseDto(
        beløp = parseInt(TilkjentYtelseDomenebegrep.BELØP, rad),
        inntekt = parseValgfriInt(TilkjentYtelseDomenebegrep.INNTEKT, rad) ?: 0,
        inntektsreduksjon = parseValgfriInt(TilkjentYtelseDomenebegrep.INNTEKTSREDUKSJON, rad) ?: 0,
        samordningsfradrag = parseValgfriInt(TilkjentYtelseDomenebegrep.INNTEKT, rad) ?: 0,
        fraOgMed = parseÅrMåned(Domenebegrep.FRA_DATO, rad).atDay(1),
        tilOgMed = parseÅrMåned(Domenebegrep.TIL_DATO, rad).atEndOfMonth(),
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
        val periodeId: Long,
        val forrigePeriodeId: Long?,
        val sats: Int,
        val satsType: SatsType,
        val fom: LocalDate,
        val tom: LocalDate,
        val opphør: LocalDate?
    )

    data class ForventetTilkjentYtelse(
        val behandlingId: UUID,
        val andeler: List<ForventetAndelTilkjentYtelse>,
        val startdato: LocalDate
    )

    data class ForventetAndelTilkjentYtelse(
        val fom: LocalDate,
        val tom: LocalDate,
        val periodeId: Long,
        val forrigePeriodeId: Long?,
        val beløp: Int,
        val kildeBehandlingId: UUID?
    )
}

enum class TilkjentYtelseDomenebegrep(override val nøkkel: String) : Domenenøkkel {
    STARTDATO("Startdato"),
    INNTEKT("INNTEKT"),
    INNTEKTSREDUKSJON("Inntektsreduksjon"),
    SAMORDNINGSFRADRAG("Samordningsfradrag"),
    BELØP("Beløp"),
    PERIODETYPE("Periodetype"),
    PERIODE_ID("Periode id"),
    FORRIGE_PERIODE_ID("Forrige periode id"),
    KILDE_BEHANDLING_ID("Kilde behandling id")
    ;
}

enum class UtbetalingsoppdragDomenebegrep(override val nøkkel: String) : Domenenøkkel {
    KODE_ENDRING("Kode endring"),
    ER_ENDRING("Er endring"),
    PERIODE_ID("Periode id"),
    FORRIGE_PERIODE_ID("Forrige periode id"),
    BELØP("Beløp"),
    TYPE("Type"),
    OPPHØRSDATO("Opphørsdato")
}
