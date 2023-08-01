package no.nav.familie.ef.iverksett.brukernotifikasjon

import io.mockk.mockk
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.familie.ef.iverksett.lagIverksettData
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.YearMonth

class BrukernotifikasjonKafkaProducerTest {

    private val kafkaTemplate = mockk<KafkaTemplate<NokkelInput, BeskjedInput>>()
    private val brukernotifikasjonKafkaProducer = BrukernotifikasjonKafkaProducer(kafkaTemplate)

    private val forventetGOmregningTekst = G_OMREGNING_MELDING_TIL_BRUKER

    @Test
    fun `lagBeskjed genererer riktig melding`() {
        val iverksettRevurderingInnvilget = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andelsdatoer = listOf(
                YearMonth.now(),
                YearMonth.now().plusMonths(1),
            ),
        )
        Assertions.assertThat(brukernotifikasjonKafkaProducer.lagBeskjed(iverksettRevurderingInnvilget).getTekst()).isEqualTo(forventetGOmregningTekst)
    }
}
