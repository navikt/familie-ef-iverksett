package no.nav.familie.ef.iverksett.behandling

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.util.opprettAndelTilkjentYtelse
import no.nav.familie.ef.iverksett.util.opprettIverksett
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class BehandlingServiceTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val behandlingService = BehandlingService(iverksettingRepository)
    private val andelTilkjentYtelseUtenInntekt = opprettAndelTilkjentYtelse(LocalDate.of(2021,1,1), LocalDate.of(2021,12, 31), 0)
    private val andelTilkjentYtelse = opprettAndelTilkjentYtelse(LocalDate.of(2022,1,1), LocalDate.of(2024,12, 31), 400000)
    private val iverksetting = opprettIverksett(UUID.randomUUID(), andeler = listOf(andelTilkjentYtelseUtenInntekt, andelTilkjentYtelse))


    @BeforeEach
    fun setUp() {
        every { iverksettingRepository.hentAvEksternId(1) } returns iverksetting
    }

    @Test
    fun `finn inntekt for tidspunkt`() {

        val ingenAndelInnenGittTidspunkt = behandlingService.hentBeregnetInntektForBehandlingOgDato(1, LocalDate.of(2020, 1, 26))
        Assertions.assertThat(ingenAndelInnenGittTidspunkt).isNull()
        val inntektPaaTidspunktUtenInntekt = behandlingService.hentBeregnetInntektForBehandlingOgDato(1, LocalDate.of(2021, 1, 1))
        Assertions.assertThat(inntektPaaTidspunktUtenInntekt).isEqualTo(0)
        val inntektPaaTidspunktMedInntekt = behandlingService.hentBeregnetInntektForBehandlingOgDato(1, LocalDate.of(2022, 12, 31))
        Assertions.assertThat(inntektPaaTidspunktMedInntekt).isEqualTo(400000)

    }

}