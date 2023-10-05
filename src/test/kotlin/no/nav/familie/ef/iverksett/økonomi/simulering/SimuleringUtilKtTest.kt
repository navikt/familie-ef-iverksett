package no.nav.familie.ef.iverksett.økonomi.simulering

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.iverksett.posteringer
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FagOmrådeKode
import no.nav.familie.kontrakter.felles.simulering.MottakerType
import no.nav.familie.kontrakter.felles.simulering.SimuleringMottaker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SimuleringUtilKtTest {

    val simuleringsResultat = mockk<DetaljertSimuleringResultat>()

    @Test
    fun `summer flere manuelle posteringer fagOmrådeKode ENSLIG_FORSØRGER_OVERGANGSSTØNAD_MANUELL_POSTERING`() {
        val simulertePosteringer = posteringer(fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD_MANUELL_POSTERING, beløp = 1, antallMåneder = 3)
        val simulertePosteringerAnnenFagOmrådeKode = posteringer(fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD, beløp = 1, antallMåneder = 3)
        val simuleringsMottaker = listOf(SimuleringMottaker(simulertPostering = simulertePosteringer + simulertePosteringerAnnenFagOmrådeKode, mottakerType = MottakerType.BRUKER))

        every { simuleringsResultat.simuleringMottaker } returns simuleringsMottaker

        assertThat(summerManuellePosteringer(simuleringsResultat)).isEqualTo(BigDecimal(3))
    }

    @Test
    fun `ikke summer posteringer med fagOmrådeKode ulik ENSLIG_FORSØRGER_OVERGANGSSTØNAD_MANUELL_POSTERING`() {
        val simulertePosteringer = posteringer(fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD, beløp = 1, antallMåneder = 3)
        val simuleringsMottaker = listOf(SimuleringMottaker(simulertPostering = simulertePosteringer, mottakerType = MottakerType.BRUKER))
        every { simuleringsResultat.simuleringMottaker } returns simuleringsMottaker

        assertThat(summerManuellePosteringer(simuleringsResultat)).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `summerManuellePosteringer skal returnere ZERO by default `() {
        every { simuleringsResultat.simuleringMottaker } returns emptyList()
        assertThat(summerManuellePosteringer(simuleringsResultat)).isEqualTo(BigDecimal.ZERO)
    }
}
