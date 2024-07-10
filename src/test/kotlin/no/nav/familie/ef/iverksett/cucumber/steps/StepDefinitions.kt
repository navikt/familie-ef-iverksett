package no.nav.familie.ef.iverksett.cucumber.steps

import io.cucumber.datatable.DataTable
import io.cucumber.java.no.Gitt
import io.cucumber.java.no.Når
import io.cucumber.java.no.Så
import io.mockk.mockk
import no.nav.familie.ef.iverksett.cucumber.domeneparser.IdTIlUUIDHolder
import no.nav.familie.ef.iverksett.cucumber.domeneparser.TilkjentYtelseParser
import no.nav.familie.ef.iverksett.cucumber.domeneparser.parseDato
import no.nav.familie.ef.iverksett.cucumber.domeneparser.parseÅrMåned
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.oppgave.OppgaveService
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsperiode
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class TilkjentYtelseHolder(
    val behandlingId: UUID,
    val behandlingIdInt: Int,
    val tilkjentYtelse: TilkjentYtelseDto,
)

class StepDefinitions {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var stønadType: StønadType
    private var tilkjentYtelse = mutableListOf<TilkjentYtelseHolder>()
    private var startdato = mapOf<UUID, LocalDate>()
    private lateinit var vedtaksdato: LocalDate
    private var fristForInntektsjekk: LocalDate? = LocalDate.now()
    private var beregnedeTilkjentYtelse = mapOf<UUID, TilkjentYtelse>()

    @Gitt("følgende startdatoer")
    fun følgendeStartdatoer(dataTable: DataTable) {
        startdato = TilkjentYtelseParser.mapStartdatoer(dataTable)
    }

    @Gitt("følgende vedtaksdato {}")
    fun følgendeVedtaksdato(vedtaksdatoArg: String) {
        vedtaksdato = parseDato(vedtaksdatoArg)
    }

    @Gitt("følgende tilkjente ytelser for {}")
    fun følgendeVedtak(
        stønadTypeArg: String,
        dataTable: DataTable,
    ) {
        stønadType = StønadType.valueOf(stønadTypeArg.uppercase())
        tilkjentYtelse.addAll(TilkjentYtelseParser.mapTilkjentYtelse(dataTable, startdato))
    }

    @Gitt("følgende tilkjente ytelser uten andel for {}")
    fun `følgende tilkjente ytelser uten andel for`(
        stønadTypeArg: String,
        dataTable: DataTable,
    ) {
        stønadType = StønadType.valueOf(stønadTypeArg.uppercase())
        tilkjentYtelse.addAll(TilkjentYtelseParser.mapTilkjentYtelse(dataTable, startdato, false))
    }

    @Når("lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception")
    fun `lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception`() {
        catchThrowable { `andelhistorikk kjøres`() }
    }

    @Når("lag frist for ferdigstillelse av inntektsjekk")
    fun `lag frist for ferdigstillelse av inntektsjekk`() {
        fristForInntektsjekk = OppgaveService(mockk(), mockk(), mockk()).lagFristFerdigstillelseFremleggsoppgaver(vedtaksdato)
    }

    @Når("lagTilkjentYtelseMedUtbetalingsoppdrag kjøres")
    fun `andelhistorikk kjøres`() {
        beregnedeTilkjentYtelse =
            tilkjentYtelse
                .fold(emptyList<Pair<UUID, TilkjentYtelse>>()) { acc, holder ->
                    val nyTilkjentYtelseMedMetaData = toMedMetadata(holder, stønadType)
                    val forrigeTilkjentYtelse = acc.lastOrNull()?.second
                    val nyTilkjentYtelse =
                        UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdragNy(
                            nyTilkjentYtelseMedMetaData,
                            forrigeTilkjentYtelse,
                        )
                    acc + (holder.behandlingId to justerPeriodeId(forrigeTilkjentYtelse, nyTilkjentYtelse))
                }.toMap()
    }

    /**
     * Pga at nye utbetalingsgeneratorn begynner med periodeId på 0, så justeres periodeIdn her for å unngå å endre alle tester med periodeIdn nå
     * Det trenger vi kun å justere for førstegangsbehandling som setter "startPeriodeId"
     */
    private fun justerPeriodeId(
        forrigeTilkjentYtelse: TilkjentYtelse?,
        nyTilkjentYtelse: TilkjentYtelse,
    ): TilkjentYtelse =
        if (forrigeTilkjentYtelse?.sisteAndelIKjede != null) {
            nyTilkjentYtelse
        } else {
            nyTilkjentYtelse.copy(
                andelerTilkjentYtelse =
                    nyTilkjentYtelse.andelerTilkjentYtelse.map {
                        it.copy(
                            periodeId = it.periodeId?.plus(1),
                            forrigePeriodeId = it.forrigePeriodeId?.plus(1),
                        )
                    },
                utbetalingsoppdrag =
                    nyTilkjentYtelse.utbetalingsoppdrag?.let { utbetalingsoppdrag ->
                        utbetalingsoppdrag.copy(
                            utbetalingsperiode =
                                utbetalingsoppdrag.utbetalingsperiode.map {
                                    it.copy(
                                        periodeId = it.periodeId + 1,
                                        forrigePeriodeId = it.forrigePeriodeId?.plus(1),
                                    )
                                },
                        )
                    },
                sisteAndelIKjede =
                    nyTilkjentYtelse.sisteAndelIKjede?.let {
                        it.copy(
                            periodeId = it.periodeId?.plus(1),
                            forrigePeriodeId = it.forrigePeriodeId?.plus(1),
                        )
                    },
            )
        }

    @Så("forvent frist satt til {}")
    fun `forvent følgende frist`(forventetFrist: String) {
        assertThat(fristForInntektsjekk).isEqualTo(parseDato(forventetFrist))
    }

    @Så("forvent følgende utbetalingsoppdrag uten utbetalingsperiode")
    fun `forvent følgende utbetalingsoppdrag uten utbetalingsperiode`(dataTable: DataTable) {
        val forventedeUtbetalingsoppdrag = TilkjentYtelseParser.mapForventetUtbetalingsoppdrag(dataTable, false)
        assertSjekkBehandlingIder(forventedeUtbetalingsoppdrag.map { it.behandlingId }, false)
        forventedeUtbetalingsoppdrag.forEach { forventetUtbetalingsoppdrag ->
            val utbetalingsoppdrag = (
                beregnedeTilkjentYtelse[forventetUtbetalingsoppdrag.behandlingId]?.utbetalingsoppdrag
                    ?: error("Mangler utbetalingsoppdrag for ${forventetUtbetalingsoppdrag.behandlingId}")
            )
            assertUtbetalingsoppdrag(forventetUtbetalingsoppdrag, utbetalingsoppdrag, false)
        }
    }

    @Så("forvent følgende utbetalingsoppdrag")
    fun `forvent følgende utbetalingsoppdrag`(dataTable: DataTable) {
        val forventedeUtbetalingsoppdrag = TilkjentYtelseParser.mapForventetUtbetalingsoppdrag(dataTable)
        assertSjekkBehandlingIder(forventedeUtbetalingsoppdrag.map { it.behandlingId })

        forventedeUtbetalingsoppdrag.forEachIndexed { index, forventetUtbetalingsoppdrag ->
            val utbetalingsoppdrag = (
                beregnedeTilkjentYtelse[forventetUtbetalingsoppdrag.behandlingId]?.utbetalingsoppdrag
                    ?: error("Mangler utbetalingsoppdrag for ${forventetUtbetalingsoppdrag.behandlingId}")
            )
            try {
                assertUtbetalingsoppdrag(forventetUtbetalingsoppdrag, utbetalingsoppdrag)
            } catch (e: Throwable) {
                logger.error("Feilet forventet utbetalingsoppdrag index=$index")
                throw e
            }
        }
    }

    @Så("forvent følgende tilkjente ytelser for behandling {int}")
    fun `forvent følgende tilkjente ytelser`(
        behandlingId: Int,
        dataTable: DataTable,
    ) {
        `forvent følgende tilkjente ytelser med startdato`(behandlingId, null, dataTable)
    }

    @Så("forvent følgende tilkjente ytelser for behandling {int} med startdato {}")
    fun `forvent følgende tilkjente ytelser med startdato`(
        behandlingIdInt: Int,
        startdato: String?,
        dataTable: DataTable,
    ) {
        val parsedStartdato = startdato?.let { parseÅrMåned(it) }
        val behandlingId = IdTIlUUIDHolder.behandlingIdTilUUID[behandlingIdInt]!!
        val forventetTilkjentYtelse =
            TilkjentYtelseParser.mapForventetTilkjentYtelse(dataTable, behandlingIdInt, parsedStartdato)
        val beregnetTilkjentYtelse =
            beregnedeTilkjentYtelse[behandlingId] ?: error("Mangler beregnet tilkjent ytelse for $behandlingIdInt")

        assertTilkjentYtelse(forventetTilkjentYtelse, beregnetTilkjentYtelse)
    }

    @Så("forvent følgende tilkjente ytelser med tomme andeler for behandling {int} og startdato {}")
    fun `forvent følgende tilkjente ytelser med tomme andeler for behandling og startdato`(
        behandlingIdInt: Int,
        startdato: String?,
    ) {
        val parsedStartdato = startdato?.let { parseÅrMåned(it) }
        val behandlingId = IdTIlUUIDHolder.behandlingIdTilUUID[behandlingIdInt]!!
        val beregnetTilkjentYtelse =
            beregnedeTilkjentYtelse[behandlingId] ?: error("Mangler beregnet tilkjent ytelse for $behandlingIdInt")

        assertTilkjentYtelseMed0BeløpAndeler(behandlingId, parsedStartdato, beregnetTilkjentYtelse)
    }

    private fun assertTilkjentYtelseMed0BeløpAndeler(
        behandlingId: UUID,
        startmåned: YearMonth?,
        beregnetTilkjentYtelse: TilkjentYtelse,
    ) {
        assertThat(beregnetTilkjentYtelse.startmåned).isEqualTo(startmåned)
        assertThat(beregnetTilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(beregnetTilkjentYtelse.andelerTilkjentYtelse.first().beløp).isEqualTo(0)
        assertThat(
            beregnetTilkjentYtelse.andelerTilkjentYtelse
                .first()
                .periode.fom,
        ).isEqualTo(YearMonth.from(LocalDate.MIN))
        assertThat(
            beregnetTilkjentYtelse.andelerTilkjentYtelse
                .first()
                .periode.tom,
        ).isEqualTo(YearMonth.from(LocalDate.MIN))
        assertThat(beregnetTilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isNull()
        assertThat(beregnetTilkjentYtelse.andelerTilkjentYtelse.first().kildeBehandlingId).isEqualTo(behandlingId)
    }

    private fun assertTilkjentYtelse(
        forventetTilkjentYtelse: TilkjentYtelseParser.ForventetTilkjentYtelse,
        beregnetTilkjentYtelse: TilkjentYtelse,
    ) {
        beregnetTilkjentYtelse.andelerTilkjentYtelse.forEachIndexed { index, andel ->
            try {
                val forventetAndel = forventetTilkjentYtelse.andeler[index]
                assertThat(andel.periode.fom).isEqualTo(forventetAndel.fom)
                assertThat(andel.periode.tom).isEqualTo(forventetAndel.tom)
                assertThat(andel.beløp).isEqualTo(forventetAndel.beløp)
                assertThat(andel.periodeId).isEqualTo(forventetAndel.periodeId)
                assertThat(andel.forrigePeriodeId).isEqualTo(forventetAndel.forrigePeriodeId)
                if (forventetAndel.kildeBehandlingId != null) {
                    assertThat(andel.kildeBehandlingId).isEqualTo(forventetAndel.kildeBehandlingId)
                }
            } catch (e: Throwable) {
                logger.error("Feilet assertTilkjentYtelse index=$index")
                throw e
            }
        }
        assertThat(beregnetTilkjentYtelse.startmåned).isEqualTo(forventetTilkjentYtelse.startmåned)
        assertThat(beregnetTilkjentYtelse.andelerTilkjentYtelse).hasSize(forventetTilkjentYtelse.andeler.size)
    }

    private fun assertUtbetalingsoppdrag(
        forventetUtbetalingsoppdrag: TilkjentYtelseParser.ForventetUtbetalingsoppdrag,
        utbetalingsoppdrag: Utbetalingsoppdrag,
        medUtbetalingsperiode: Boolean = true,
    ) {
        assertThat(utbetalingsoppdrag.kodeEndring).isEqualTo(forventetUtbetalingsoppdrag.kodeEndring)
        assertThat(utbetalingsoppdrag.utbetalingsperiode).hasSize(forventetUtbetalingsoppdrag.utbetalingsperiode.size)
        if (medUtbetalingsperiode) {
            forventetUtbetalingsoppdrag.utbetalingsperiode.forEachIndexed { index, forventetUtbetalingsperiode ->
                val utbetalingsperiode = utbetalingsoppdrag.utbetalingsperiode[index]
                assertUtbetalingsperiode(utbetalingsperiode, forventetUtbetalingsperiode)
            }
        }
    }

    private fun assertUtbetalingsperiode(
        utbetalingsperiode: Utbetalingsperiode,
        forventetUtbetalingsperiode: TilkjentYtelseParser.ForventetUtbetalingsperiode,
    ) {
        assertThat(utbetalingsperiode.erEndringPåEksisterendePeriode)
            .isEqualTo(forventetUtbetalingsperiode.erEndringPåEksisterendePeriode)
        assertThat(utbetalingsperiode.klassifisering).isEqualTo(Ytelsestype.valueOf(stønadType.name).kode)
        assertThat(utbetalingsperiode.periodeId).isEqualTo(forventetUtbetalingsperiode.periodeId)
        assertThat(utbetalingsperiode.forrigePeriodeId).isEqualTo(forventetUtbetalingsperiode.forrigePeriodeId)
        assertThat(utbetalingsperiode.sats.toInt()).isEqualTo(forventetUtbetalingsperiode.sats)
        assertThat(utbetalingsperiode.satsType).isEqualTo(forventetUtbetalingsperiode.satsType)
        assertThat(utbetalingsperiode.vedtakdatoFom).isEqualTo(forventetUtbetalingsperiode.fom)
        assertThat(utbetalingsperiode.vedtakdatoTom).isEqualTo(forventetUtbetalingsperiode.tom)
        assertThat(utbetalingsperiode.opphør?.opphørDatoFom).isEqualTo(forventetUtbetalingsperiode.opphør)
    }

    private fun assertSjekkBehandlingIder(
        expectedBehandlingIder: List<UUID>,
        medUtbetalingsperiode: Boolean = true,
    ) {
        val list =
            beregnedeTilkjentYtelse
                .filter {
                    it.value.utbetalingsoppdrag
                        ?.utbetalingsperiode
                        ?.isNotEmpty() == medUtbetalingsperiode
                }.map { it.key }
        assertThat(expectedBehandlingIder).containsExactlyInAnyOrderElementsOf(list)
    }
}

private fun toMedMetadata(
    holder: TilkjentYtelseHolder,
    stønadType: StønadType,
): TilkjentYtelseMedMetaData =
    holder.tilkjentYtelse
        .toDomain()
        .toMedMetadata(
            saksbehandlerId = "",
            eksternBehandlingId = holder.behandlingIdInt.toLong(),
            stønadType = stønadType,
            eksternFagsakId = 1,
            personIdent = "1",
            behandlingId = holder.behandlingId,
            vedtaksdato = LocalDate.now(),
        )
