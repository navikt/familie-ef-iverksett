package no.nav.familie.ef.iverksett.økonomi

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.PeriodeId
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.nullAndelTilkjentYtelse
import no.nav.familie.felles.utbetalingsgenerator.domain.Opphør
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsperiode
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.jupiter.api.Assertions
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val BEHANDLING_EKSTERN_ID = 0L
private const val FAGSAK_EKSTERN_ID = 1L
private const val SAKSBEHANDLER_ID = "VL"
private val vedtaksdato = LocalDate.of(2021, 5, 12)

enum class TestOppdragType {
    Input,
    Output,
    Oppdrag,
}

/**
 * OppdragId
 *  * På input er oppdragId som settes på tilkjentYtelse. For hver ny gruppe med input skal de ha samme input
 *  * På output er oppdragId som sjekker att andelTilkjentYtelse har fått riktig output
 *  * På oppdrag trengs den ikke
 */
data class TestOppdrag(
    val type: TestOppdragType,
    val fnr: String,
    val oppdragId: UUID?,
    val ytelse: String,
    val linjeId: Long? = null,
    val forrigeLinjeId: Long? = null,
    val status110: String? = null,
    val erEndring: Boolean? = null,
    val opphørsdato: LocalDate?,
    val beløp: Int? = null,
    val startPeriode: YearMonth? = null,
    val sluttPeriode: YearMonth? = null,
) {
    fun tilAndelTilkjentYtelse(): AndelTilkjentYtelse? {
        return if (beløp != null && startPeriode != null && sluttPeriode != null) {
            lagAndelTilkjentYtelse(
                beløp = this.beløp,
                fraOgMed = startPeriode,
                tilOgMed = sluttPeriode,
                periodeId = linjeId,
                kildeBehandlingId = if (TestOppdragType.Output == type) oppdragId else null,
                forrigePeriodeId = forrigeLinjeId,
            )
        } else if (TestOppdragType.Output == type && beløp == null && startPeriode == null && sluttPeriode == null) {
            nullAndelTilkjentYtelse(
                kildeBehandlingId = oppdragId ?: error("Må ha satt OppdragId på Output"),
                periodeId = PeriodeId(linjeId, forrigeLinjeId),
            )
        } else {
            null
        }
    }

    fun tilUtbetalingsperiode(): Utbetalingsperiode? {
        return if (startPeriode != null && sluttPeriode != null && linjeId != null) {
            Utbetalingsperiode(
                erEndringPåEksisterendePeriode = erEndring ?: false,
                opphør = opphørsdato?.let { Opphør(it) },
                periodeId = linjeId,
                forrigePeriodeId = forrigeLinjeId,
                datoForVedtak = vedtaksdato,
                klassifisering = ytelse,
                vedtakdatoFom = startPeriode.atDay(1),
                vedtakdatoTom = sluttPeriode.atEndOfMonth(),
                sats = beløp?.toBigDecimal() ?: BigDecimal.ZERO,
                satsType = Utbetalingsperiode.SatsType.MND,
                utbetalesTil = fnr,
                behandlingId = 1,
                utbetalingsgrad = 100,
            )
        } else if (opphørsdato != null) {
            error("Kan ikke sette opphørsdato her, mangler start/slutt/linjeId")
        } else {
            null
        }
    }
}

class TestOppdragGroup {
    private var startdatoInn: LocalDate? = null
    private var startdatoUt: LocalDate? = null
    private val andelerTilkjentYtelseInn: MutableList<AndelTilkjentYtelse> = mutableListOf()
    private val andelerTilkjentYtelseUt: MutableList<AndelTilkjentYtelse> = mutableListOf()
    private val utbetalingsperioder: MutableList<Utbetalingsperiode> = mutableListOf()

    private var oppdragKode110: Utbetalingsoppdrag.KodeEndring = Utbetalingsoppdrag.KodeEndring.NY
    private var personIdent: String? = null
    private var oppdragId: UUID? = null

    fun add(to: TestOppdrag) {
        when (to.type) {
            TestOppdragType.Input -> {
                oppdragId = to.oppdragId
                personIdent = to.fnr
                if (to.opphørsdato != null) {
                    startdatoInn = validerOgGetStartdato(to, startdatoInn)
                }
                to.tilAndelTilkjentYtelse()?.also { andelerTilkjentYtelseInn.add(it) }
            }
            TestOppdragType.Oppdrag -> {
                oppdragKode110 = Utbetalingsoppdrag.KodeEndring.valueOf(to.status110!!)
                to.tilUtbetalingsperiode()?.also { utbetalingsperioder.add(it) }
            }
            TestOppdragType.Output -> {
                startdatoUt = validerOgGetStartdato(to, startdatoUt)
                // Vi lagrer ned en nullandel for output
                to.tilAndelTilkjentYtelse()?.also { andelerTilkjentYtelseUt.add(it) }
            }
        }
    }

    private fun validerOgGetStartdato(
        to: TestOppdrag,
        tidligereStartdato: LocalDate?,
    ): LocalDate? {
        if (tidligereStartdato != null && to.opphørsdato != null) {
            error("Kan kun sette 1 startdato på en input/output")
        }
        return tidligereStartdato ?: to.opphørsdato
    }

    val input: TilkjentYtelseMedMetaData by lazy {
        val startmåned =
            startdatoInn?.let { YearMonth.from(it) }
                ?: andelerTilkjentYtelseInn.minOfOrNull { it.periode.fom }
                ?: error("Input feiler - hvis man ikke har en andel må man sette startdato")
        TilkjentYtelseMedMetaData(
            TilkjentYtelse(
                andelerTilkjentYtelse = andelerTilkjentYtelseInn,
                startmåned = startmåned,
            ),
            stønadstype = StønadType.OVERGANGSSTØNAD,
            eksternBehandlingId = BEHANDLING_EKSTERN_ID,
            eksternFagsakId = FAGSAK_EKSTERN_ID,
            saksbehandlerId = SAKSBEHANDLER_ID,
            personIdent = personIdent!!,
            behandlingId = oppdragId!!,
            vedtaksdato = vedtaksdato,
        )
    }

    val output: TilkjentYtelse by lazy {
        val startmåned =
            startdatoUt?.let { YearMonth.from(it) }
                ?: andelerTilkjentYtelseUt.minOfOrNull { it.periode.fom }
                ?: error("Output feiler - hvis man ikke har en andel må man sette startdato")
        val utbetalingsoppdrag =
            Utbetalingsoppdrag(
                kodeEndring = oppdragKode110,
                fagSystem = "EFOG",
                saksnummer = FAGSAK_EKSTERN_ID.toString(),
                aktoer = personIdent!!,
                saksbehandlerId = SAKSBEHANDLER_ID,
                avstemmingTidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS),
                utbetalingsperiode =
                    utbetalingsperioder
                        .map { it.copy(behandlingId = BEHANDLING_EKSTERN_ID) },
            )

        TilkjentYtelse(
            id = input.tilkjentYtelse.id,
            andelerTilkjentYtelse = andelerTilkjentYtelseUt,
            utbetalingsoppdrag = utbetalingsoppdrag,
            startmåned = startmåned,
        )
    }
}

object TestOppdragParser {
    private const val KEY_TYPE = "Type"
    private const val KEY_FNR = "Fnr"
    private const val KEY_OPPDRAG = "Oppdrag"
    private const val KEY_YTELSE = "Ytelse"
    private const val KEY_LINJE_ID = "LID"
    private const val KEY_FORRIGE_LINJE_ID = "Pre-LID"
    private const val KEY_STATUS_OPPDRAG = "Status oppdrag"
    private const val KEY_ER_ENDRING = "Er endring"

    private val RESERVERED_KEYS =
        listOf(
            KEY_TYPE,
            KEY_FNR,
            KEY_OPPDRAG,
            KEY_YTELSE,
            KEY_LINJE_ID,
            KEY_FORRIGE_LINJE_ID,
            KEY_STATUS_OPPDRAG,
            KEY_ER_ENDRING,
        )

    private val oppdragIdn = mutableMapOf<Int, UUID>()

    private fun parse(url: URL): List<TestOppdrag> {
        val fileContent = url.openStream()!!
        val rows: List<Map<String, String>> =
            csvReader().readAllWithHeader(fileContent)
                .filterNot { it.getValue(KEY_TYPE).startsWith("!") }

        return rows.map { row ->
            val datoKeysMedBeløp =
                row.keys
                    .filter { key -> !RESERVERED_KEYS.contains(key) }
                    .filter { datoKey -> (row[datoKey])?.trim('x')?.toIntOrNull() != null }
                    .sorted()

            val opphørYearMonth =
                row.keys
                    .filter { key -> !RESERVERED_KEYS.contains(key) }
                    .sorted()
                    .firstOrNull { datoKey -> (row[datoKey])?.contains('x') ?: false }
                    ?.let { YearMonth.parse(it) }

            val firstYearMonth = datoKeysMedBeløp.firstOrNull()?.let { YearMonth.parse(it) }
            val lastYearMonth = datoKeysMedBeløp.lastOrNull()?.let { YearMonth.parse(it) }
            val beløp = datoKeysMedBeløp.firstOrNull()?.let { row[it]?.trim('x') }?.toIntOrNull()

            val value = row.getValue(KEY_OPPDRAG)
            val oppdragId: UUID? =
                if (value.isEmpty()) {
                    null
                } else {
                    oppdragIdn.getOrPut(value.toInt()) { UUID.randomUUID() }
                }

            TestOppdrag(
                type = row[KEY_TYPE]?.let { TestOppdragType.valueOf(it) }!!,
                fnr = row.getValue(KEY_FNR),
                oppdragId = oppdragId,
                ytelse = row.getValue(KEY_YTELSE),
                linjeId = row[KEY_LINJE_ID]?.let { emptyAsNull(it) }?.let { Integer.parseInt(it).toLong() },
                forrigeLinjeId =
                    row[KEY_FORRIGE_LINJE_ID]
                        ?.let { emptyAsNull(it) }
                        ?.let { Integer.parseInt(it).toLong() },
                status110 = row[KEY_STATUS_OPPDRAG]?.let { emptyAsNull(it) },
                erEndring = row[KEY_ER_ENDRING]?.let { it.toBoolean() },
                beløp = beløp,
                opphørsdato = opphørYearMonth?.atDay(1),
                startPeriode = firstYearMonth,
                sluttPeriode = lastYearMonth,
            )
        }
    }

    fun parseToTestOppdragGroup(url: URL): List<TestOppdragGroup> {
        val result: MutableList<TestOppdragGroup> = mutableListOf()

        var newGroup = true

        parse(url).forEachIndexed { index, to ->
            try {
                when (to.type) {
                    TestOppdragType.Input -> {
                        if (newGroup) {
                            result.add(TestOppdragGroup())
                            newGroup = false
                        }
                    }
                    else -> {
                        newGroup = true
                    }
                }
                result.last().add(to)
            } catch (e: Exception) {
                throw RuntimeException("Feilet index=$index - ${e.message}", e)
            }
        }

        return result
    }

    private fun emptyAsNull(s: String): String? = s.ifEmpty { null }
}

object TestOppdragRunner {
    fun run(url: URL?) {
        if (url == null) error("Url Mangler")
        val grupper = TestOppdragParser.parseToTestOppdragGroup(url)

        var forrigeTilkjentYtelse: TilkjentYtelse? = null

        val om = objectMapper.writerWithDefaultPrettyPrinter()
        grupper.forEachIndexed { indeks, gruppe ->
            val input = gruppe.input
            val faktisk: TilkjentYtelse
            try {
                faktisk = lagTilkjentYtelseMedUtbetalingsoppdrag(input, forrigeTilkjentYtelse)
            } catch (e: Exception) {
                throw RuntimeException("Feilet indeks=$indeks - ${e.message}", e)
            }
            Assertions.assertEquals(
                om.writeValueAsString(truncateAvstemmingDato(gruppe.output)),
                om.writeValueAsString(truncateAvstemmingDato(faktisk)),
                "Feiler for gruppe med indeks $indeks",
            )
            forrigeTilkjentYtelse = faktisk
        }
    }

    private fun truncateAvstemmingDato(tilkjentYtelse: TilkjentYtelse): TilkjentYtelse {
        val utbetalingsoppdrag = tilkjentYtelse.utbetalingsoppdrag ?: return tilkjentYtelse
        val nyAvstemmingsitdspunkt = utbetalingsoppdrag.avstemmingTidspunkt.truncatedTo(ChronoUnit.HOURS)
        return tilkjentYtelse.copy(
            utbetalingsoppdrag = utbetalingsoppdrag.copy(avstemmingTidspunkt = nyAvstemmingsitdspunkt),
            sisteAndelIKjede = null,
        )
    }

    private fun lagTilkjentYtelseMedUtbetalingsoppdrag(
        nyTilkjentYtelse: TilkjentYtelseMedMetaData,
        forrigeTilkjentYtelse: TilkjentYtelse? = null,
    ) = UtbetalingsoppdragGenerator
        .lagTilkjentYtelseMedUtbetalingsoppdragNy(
            nyTilkjentYtelse,
            forrigeTilkjentYtelse,
        )
}
