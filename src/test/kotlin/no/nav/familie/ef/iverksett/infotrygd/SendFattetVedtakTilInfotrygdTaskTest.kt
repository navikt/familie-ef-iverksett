package no.nav.familie.ef.iverksett.infotrygd

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.infotrygd.OpprettVedtakHendelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.personopplysning.PersonIdentMedHistorikk
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class SendFattetVedtakTilInfotrygdTaskTest {

    private val infotrygdFeedClient = mockk<InfotrygdFeedClient>(relaxed = true)
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>(relaxed = true)
    private val iverksettingRepository = mockk<IverksettingRepository>()

    private val task =
            SendFattetVedtakTilInfotrygdTask(infotrygdFeedClient, familieIntegrasjonerClient, iverksettingRepository, mockk())

    private val behandlingId = UUID.randomUUID()
    private val iverksett = opprettIverksettOvergangsstønad(behandlingId)
    private val personIdent = iverksett.søker.personIdent
    private val historiskPersonIdent = "2"
    private val perioder = listOf(
            Pair(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
            Pair(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 31)),
            Pair(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31))
    )

    private val identer = listOf(PersonIdentMedHistorikk(personIdent, false),
                                 PersonIdentMedHistorikk(historiskPersonIdent, true))

    @Test
    internal fun `skal sende fattet vedtak til infotrygd med første perioden i andelene`() {
        val hendelseSlot = slot<OpprettVedtakHendelseDto>()
        every { iverksettingRepository.hent(behandlingId) } returns opprettIverksettMedTilkjentYtelse()
        every { infotrygdFeedClient.opprettVedtakHendelse(capture(hendelseSlot)) } just runs
        every { familieIntegrasjonerClient.hentIdenter(any(), any()) } returns identer

        task.doTask(Task(SendFattetVedtakTilInfotrygdTask.TYPE, behandlingId.toString()))

        assertThat(hendelseSlot.captured.personIdenter).containsExactlyInAnyOrder(personIdent, historiskPersonIdent)
        assertThat(hendelseSlot.captured.type).isEqualTo(StønadType.OVERGANGSSTØNAD)
        assertThat(hendelseSlot.captured.startdato).isEqualTo(LocalDate.of(2020, 1, 1))
    }

    private fun opprettIverksettMedTilkjentYtelse(): Iverksett {
        val vedtak = iverksett.vedtak
        val tilkjentYtelse = vedtak.tilkjentYtelse
        val andelerTilkjentYtelse = perioder.map {
            lagAndelTilkjentYtelse(1, it.first, it.second)
        }

        val nyTilkjentYtelse = tilkjentYtelse!!.copy(andelerTilkjentYtelse = andelerTilkjentYtelse)
        return iverksett.copy(vedtak = vedtak.copy(tilkjentYtelse = nyTilkjentYtelse))
    }
}