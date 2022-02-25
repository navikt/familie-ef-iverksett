package no.nav.familie.ef.iverksett.iverksetting.tilstand

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.tilstand.TilstandPatch.TilstandPatchData
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.PeriodeId
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.nullAndelTilkjentYtelse
import no.nav.familie.kontrakter.felles.oppdrag.Utbetalingsoppdrag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class TilstandPatchTest {

    private val tilstandRepository = mockk<TilstandRepository>()
    private val tilstandPatch = TilstandPatch(mockk(), tilstandRepository)

    private val slots = mutableListOf<TilkjentYtelse>()

    private val tid1 = LocalDateTime.now()
    private val tid2 = tid1.plusDays(1)
    private val tid3 = tid1.plusDays(2)
    private val tid4 = tid1.plusDays(3)

    private val andel1 = lagAndelTilkjentYtelse(1,
                                                fraOgMed = LocalDate.of(2021, 1, 1),
                                                tilOgMed = LocalDate.of(2021, 1, 31),
                                                periodeId = 1)
    private val nullAndel = nullAndelTilkjentYtelse(UUID.randomUUID(), PeriodeId(null, null))
    private val andel2 = lagAndelTilkjentYtelse(2,
                                                fraOgMed = LocalDate.of(2021, 3, 1),
                                                tilOgMed = LocalDate.of(2021, 3, 31),
                                                periodeId = 2)

    @BeforeEach
    internal fun setUp() {
        slots.clear()
        every { tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(any(), capture(slots)) } just Runs
    }

    @Test
    internal fun `setter siste andel på fagsak der det kun finnes 1 behandling`() {
        tilstandPatch.håndterGruppe(listOf(lagData(andel1, andel2, andel1)))
        assertThat(slots.single().sisteAndelIKjede).isEqualTo(andel2)
    }

    @Test
    internal fun `bruker forrige andel hvis den siste er null`() {
        tilstandPatch.håndterGruppe(listOf(lagData(andel1),
                                           lagData(nullAndel, tid = tid2),
                                           lagData(nullAndel, tid = tid3),
                                           lagData(andel2, tid = tid4)))
        assertThat(slots).hasSize(4)
        assertThat(slots[0].sisteAndelIKjede).isEqualTo(andel1)
        assertThat(slots[1].sisteAndelIKjede).isEqualTo(andel1)
        assertThat(slots[2].sisteAndelIKjede).isEqualTo(andel1)
        assertThat(slots[3].sisteAndelIKjede).isEqualTo(andel2)
    }

    @Test
    internal fun `setter andel2 til andre behandlingen då den sitt periodeId er større enn andel1 sin `() {
        tilstandPatch.håndterGruppe(listOf(lagData(andel1, andel2),
                                           lagData(andel1, tid = tid2),
                                           lagData(nullAndel, tid = tid3)))
        assertThat(slots).hasSize(3)
        assertThat(slots[0].sisteAndelIKjede).isEqualTo(andel2)
        assertThat(slots[1].sisteAndelIKjede).isEqualTo(andel2)
        assertThat(slots[2].sisteAndelIKjede).isEqualTo(andel2)
    }

    private fun lagData(vararg andel: AndelTilkjentYtelse, tid: LocalDateTime = tid1): TilstandPatchData {
        val tilkjentYtelse = TilkjentYtelse(andelerTilkjentYtelse = andel.toList(),
                                            startdato = null,
                                            utbetalingsoppdrag = utbetalingsoppdrag(tid))
        return TilstandPatchData(TilstandPatch.FagsakId(UUID.randomUUID()), UUID.randomUUID(), tilkjentYtelse)
    }

    fun utbetalingsoppdrag(localDateTime: LocalDateTime) =
            Utbetalingsoppdrag(kodeEndring = Utbetalingsoppdrag.KodeEndring.NY,
                               fagSystem = "",
                               saksnummer = "",
                               aktoer = "",
                               saksbehandlerId = "",
                               avstemmingTidspunkt = localDateTime,
                               utbetalingsperiode = emptyList())
}