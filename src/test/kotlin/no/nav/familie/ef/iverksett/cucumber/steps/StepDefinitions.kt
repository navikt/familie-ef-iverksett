package no.nav.familie.ef.iverksett.cucumber.steps

import io.cucumber.datatable.DataTable
import io.cucumber.java.no.Gitt
import io.cucumber.java.no.Når
import io.cucumber.java.no.Så
import no.nav.familie.ef.iverksett.cucumber.domeneparser.IdTIlUUIDHolder
import no.nav.familie.ef.iverksett.cucumber.domeneparser.TilkjentYtelseParser
import no.nav.familie.ef.iverksett.cucumber.domeneparser.parseÅrMåned
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsperiode
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelseHolder(
    val behandlingId: UUID,
    val behandlingIdInt: Int,
    val tilkjentYtelse: TilkjentYtelseDto,
)

class StepDefinitions {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var stønadType: StønadType
    private var tilkjentYtelse = listOf<TilkjentYtelseHolder>()
    private var startdato = mapOf<UUID, LocalDate>()

    private var beregnedeTilkjentYtelse = mapOf<UUID, TilkjentYtelse>()

    @Gitt("følgende startdatoer")
    fun følgende_startdatoer(dataTable: DataTable) {
        startdato = TilkjentYtelseParser.mapStartdatoer(dataTable)
    }

    @Gitt("følgende tilkjente ytelser for {}")
    fun følgende_vedtak(stønadTypeArg: String, dataTable: DataTable) {
        stønadType = StønadType.valueOf(stønadTypeArg.uppercase())
        tilkjentYtelse = TilkjentYtelseParser.mapTilkjentYtelse(dataTable, startdato)
    }

    @Når("lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception")
    fun `lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception`() {
        catchThrowable { `andelhistorikk kjøres`() }
    }

    @Når("lagTilkjentYtelseMedUtbetalingsoppdrag kjøres")
    fun `andelhistorikk kjøres`() {
        beregnedeTilkjentYtelse = tilkjentYtelse.fold(emptyList<Pair<UUID, TilkjentYtelse>>()) { acc, holder ->
            val nyTilkjentYtelseMedMetaData = toMedMetadata(holder, stønadType)
            val nyTilkjentYtelse = UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                nyTilkjentYtelseMedMetaData,
                acc.lastOrNull()?.second
            )
            acc + (holder.behandlingId to nyTilkjentYtelse)
        }.toMap()
    }

    @Så("forvent følgende utbetalingsoppdrag")
    fun `forvent følgende utbetalingsoppdrag`(dataTable: DataTable) {
        val forventedeUtbetalingsoppdrag = TilkjentYtelseParser.mapForventetUtbetalingsoppdrag(dataTable)
        assertSjekkerAlleBehandlingIder(forventedeUtbetalingsoppdrag.map { it.behandlingId })

        forventedeUtbetalingsoppdrag.forEach { forventetUtbetalingsoppdrag ->
            val utbetalingsoppdrag = (
                beregnedeTilkjentYtelse[forventetUtbetalingsoppdrag.behandlingId]?.utbetalingsoppdrag
                    ?: error("Manggler utbetalingsoppdrag for ${forventetUtbetalingsoppdrag.behandlingId}")
                )
            assertUtbetalingsoppdrag(forventetUtbetalingsoppdrag, utbetalingsoppdrag)
        }
    }

    @Så("forvent følgende tilkjente ytelser for behandling {int}")
    fun `forvent følgende tilkjente ytelser`(behandlingId: Int, dataTable: DataTable) {
        `forvent følgende tilkjente ytelser med startdato`(behandlingId, null, dataTable)
    }

    @Så("forvent følgende tilkjente ytelser for behandling {int} med startdato {}")
    fun `forvent følgende tilkjente ytelser med startdato`(behandlingIdInt: Int, startdato: String?, dataTable: DataTable) {
        val parsedStartdato = startdato?.let { parseÅrMåned(it).atDay(1) }
        val behandlingId = IdTIlUUIDHolder.behandlingIdTilUUID[behandlingIdInt]!!
        val forventetTilkjentYtelse = TilkjentYtelseParser.mapForventetTilkjentYtelse(dataTable, behandlingIdInt, parsedStartdato)
        val beregnetTilkjentYtelse =
            beregnedeTilkjentYtelse[behandlingId] ?: error("Mangler beregnet tilkjent ytelse for $behandlingIdInt")

        assertTilkjentYtelse(forventetTilkjentYtelse, beregnetTilkjentYtelse)
    }

    private fun assertTilkjentYtelse(
        forventetTilkjentYtelse: TilkjentYtelseParser.ForventetTilkjentYtelse,
        beregnetTilkjentYtelse: TilkjentYtelse
    ) {
        beregnetTilkjentYtelse.andelerTilkjentYtelse.forEachIndexed { index, andel ->
            val forventetAndel = forventetTilkjentYtelse.andeler[index]
            assertThat(andel.fraOgMed).isEqualTo(forventetAndel.fom)
            assertThat(andel.tilOgMed).isEqualTo(forventetAndel.tom)
            assertThat(andel.beløp).isEqualTo(forventetAndel.beløp)
            assertThat(andel.periodeId).isEqualTo(forventetAndel.periodeId)
            assertThat(andel.forrigePeriodeId).isEqualTo(forventetAndel.forrigePeriodeId)
        }
        assertThat(beregnetTilkjentYtelse.startdato).isEqualTo(forventetTilkjentYtelse.startdato)
        assertThat(beregnetTilkjentYtelse.andelerTilkjentYtelse).hasSize(forventetTilkjentYtelse.andeler.size)
    }

    private fun assertUtbetalingsoppdrag(
        forventetUtbetalingsoppdrag: TilkjentYtelseParser.ForventetUtbetalingsoppdrag,
        utbetalingsoppdrag: Utbetalingsoppdrag
    ) {

        assertThat(utbetalingsoppdrag.kodeEndring).isEqualTo(forventetUtbetalingsoppdrag.kodeEndring)
        assertThat(utbetalingsoppdrag.utbetalingsperiode).hasSize(forventetUtbetalingsoppdrag.utbetalingsperiode.size)
        forventetUtbetalingsoppdrag.utbetalingsperiode.forEachIndexed { index, forventetUtbetalingsperiode ->
            val utbetalingsperiode = utbetalingsoppdrag.utbetalingsperiode[index]
            assertUtbetalingsperiode(utbetalingsperiode, forventetUtbetalingsperiode)
        }
    }

    private fun assertUtbetalingsperiode(
        utbetalingsperiode: Utbetalingsperiode,
        forventetUtbetalingsperiode: TilkjentYtelseParser.ForventetUtbetalingsperiode
    ) {
        assertThat(utbetalingsperiode.erEndringPåEksisterendePeriode).isEqualTo(forventetUtbetalingsperiode.erEndringPåEksisterendePeriode)
        assertThat(utbetalingsperiode.klassifisering).isEqualTo(Ytelsestype.valueOf(stønadType.name).kode)
        assertThat(utbetalingsperiode.periodeId).isEqualTo(forventetUtbetalingsperiode.periodeId)
        assertThat(utbetalingsperiode.forrigePeriodeId).isEqualTo(forventetUtbetalingsperiode.forrigePeriodeId)
        assertThat(utbetalingsperiode.sats.toInt()).isEqualTo(forventetUtbetalingsperiode.sats)
        assertThat(utbetalingsperiode.satsType).isEqualTo(forventetUtbetalingsperiode.satsType)
        assertThat(utbetalingsperiode.vedtakdatoFom).isEqualTo(forventetUtbetalingsperiode.fom)
        assertThat(utbetalingsperiode.vedtakdatoTom).isEqualTo(forventetUtbetalingsperiode.tom)
        assertThat(utbetalingsperiode.opphør?.opphørDatoFom).isEqualTo(forventetUtbetalingsperiode.opphør)
    }

    private fun assertSjekkerAlleBehandlingIder(expectedBehandlingIder: List<UUID>) {
        assertThat(expectedBehandlingIder).containsExactlyInAnyOrderElementsOf(beregnedeTilkjentYtelse.keys)
    }
}

private fun toMedMetadata(holder: TilkjentYtelseHolder, stønadType: StønadType): TilkjentYtelseMedMetaData {
    return holder.tilkjentYtelse.toDomain()
        .toMedMetadata(
            saksbehandlerId = "",
            eksternBehandlingId = holder.behandlingIdInt.toLong(),
            stønadType = stønadType,
            eksternFagsakId = 1,
            personIdent = "1",
            behandlingId = holder.behandlingId,
            vedtaksdato = LocalDate.now(),
        )
}
