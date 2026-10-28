package no.nav.familie.ef.iverksett.cucumber.domeneparser

import io.cucumber.datatable.DataTable
import io.cucumber.datatable.DataTableTypeRegistry
import io.cucumber.datatable.DataTableTypeRegistryTableConverter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.YearMonth
import java.util.Locale

class TilkjentYtelseParserTest {
    @Test
    fun `skal tillate null i valgfrie felt`() {
        val dataTable =
            lagDataTable(
                listOf(
                    listOf("Fra dato", "Til dato", "Beløp", "Periode id", "Forrige periode id", "Kilde behandling id"),
                    listOf("01.2021", "02.2021", "700", null, null, null),
                ),
            )

        val resultat = TilkjentYtelseParser.mapForventetTilkjentYtelse(dataTable, 1, null)
        val andel = resultat.andeler.single()

        assertThat(resultat.startmåned).isEqualTo(YearMonth.of(2021, 1))
        assertThat(andel.beløp).isEqualTo(700)
        assertThat(andel.periodeId).isNull()
        assertThat(andel.forrigePeriodeId).isNull()
        assertThat(andel.kildeBehandlingId).isNull()
    }

    @Test
    fun `skal avvise null i obligatoriske felt`() {
        val dataTable =
            lagDataTable(
                listOf(
                    listOf("Beløp"),
                    listOf(null),
                ),
            )

        assertThatThrownBy { TilkjentYtelseParser.mapForventetTilkjentYtelse(dataTable, 1, YearMonth.of(2021, 1)) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Fant ingen verdi")
    }

    @Test
    fun `skal avvise null som kolonnenavn ved parsing av forventet ytelse`() {
        val dataTable = lagDataTable(listOf(listOf("Beløp", null), listOf("700", "verdi")))

        assertThatThrownBy { TilkjentYtelseParser.mapForventetTilkjentYtelse(dataTable, 1, YearMonth.of(2021, 1)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Mangler kolonnenavn i datatabell")
    }

    @Test
    fun `skal avvise null som kolonnenavn ved gruppering per behandling`() {
        val dataTable = lagDataTable(listOf(listOf("BehandlingId", null), listOf("1", "verdi")))

        assertThatThrownBy { TilkjentYtelseParser.mapStartdatoer(dataTable) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Mangler kolonnenavn i datatabell")
    }

    private fun lagDataTable(rader: List<List<String?>>): DataTable = DataTable.create(rader, DataTableTypeRegistryTableConverter(DataTableTypeRegistry(Locale.ROOT)))
}
