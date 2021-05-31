package no.nav.familie.ef.iverksett.infotrygd

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Periodebeløp
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.infotrygd.OpprettVedtakHendelseDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class SendFattetVedtakTilInfotrygdTaskTest {

    private val infotrygdFeedClient = mockk<InfotrygdFeedClient>(relaxed = true)
    private val iverksettingRepository = mockk<IverksettingRepository>()

    private val task = SendFattetVedtakTilInfotrygdTask(infotrygdFeedClient, iverksettingRepository, mockk())

    private val behandlingId = UUID.randomUUID()
    private val personIdenter = setOf("2")
    private val perioder = listOf(
            Pair(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
            Pair(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 31)),
            Pair(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31))
    )

    @Test
    internal fun `skal sende fattet vedtak til infotrygd med første perioden i andelene`() {
        val hendelseSlot = slot<OpprettVedtakHendelseDto>()
        every { iverksettingRepository.hent(behandlingId) } returns opprettIverksettMedTilkjentYtelse()
        every { infotrygdFeedClient.opprettVedtakHendelse(capture(hendelseSlot)) } just runs

        task.doTask(Task(SendFattetVedtakTilInfotrygdTask.TYPE, behandlingId.toString()))

        assertThat(hendelseSlot.captured.personIdenter).isEqualTo(personIdenter)
        assertThat(hendelseSlot.captured.type).isEqualTo(StønadType.OVERGANGSSTØNAD)
        assertThat(hendelseSlot.captured.startdato).isEqualTo(LocalDate.of(2020, 1, 1))
    }

    private fun opprettIverksettMedTilkjentYtelse(): Iverksett {
        val iverksett = opprettIverksett(behandlingId)
        val vedtak = iverksett.vedtak
        val tilkjentYtelse = vedtak.tilkjentYtelse
        val andelerTilkjentYtelse = perioder.map {
            AndelTilkjentYtelse(Periodebeløp(1, Periodetype.MÅNED, it.first, it.second))
        }

        val nyTilkjentYtelse = tilkjentYtelse.copy(andelerTilkjentYtelse = andelerTilkjentYtelse)
        return iverksett.copy(vedtak = vedtak.copy(tilkjentYtelse = nyTilkjentYtelse),
                              søker = iverksett.søker.copy(personIdenter = personIdenter))
    }
}