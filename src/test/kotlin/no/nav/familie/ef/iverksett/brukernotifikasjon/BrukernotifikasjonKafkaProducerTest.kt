package no.nav.familie.ef.iverksett.brukernotifikasjon

import io.mockk.mockk
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.familie.ef.iverksett.lagIverksettData
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.Grunnbeløp
import no.nav.familie.kontrakter.felles.Månedsperiode
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDate
import java.time.YearMonth

class BrukernotifikasjonKafkaProducerTest {

    private val kafkaTemplate = mockk<KafkaTemplate<NokkelInput, BeskjedInput>>()
    private val brukernotifikasjonKafkaProducer = BrukernotifikasjonKafkaProducer(kafkaTemplate)

    @Test
    fun `lagBeskjed genererer riktig melding`() {
        val forventetGOmregningTekst = "Fra 01.05.2023 har folketrygdens grunnbeløp økt til 118620 kroner og overgangsstønaden din er derfor endret."
        val iverksettRevurderingInnvilget = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andelsdatoer = listOf(
                YearMonth.now(),
                YearMonth.now().plusMonths(1),
            ),
            grunnbeløp = Grunnbeløp(
                periode = Månedsperiode(fom = YearMonth.of(2023, 5), tom = YearMonth.from(LocalDate.MAX)),
                grunnbeløp = 118_620.toBigDecimal(),
            ),
        )
        Assertions.assertThat(brukernotifikasjonKafkaProducer.lagBeskjed(iverksettRevurderingInnvilget).getTekst()).isEqualTo(forventetGOmregningTekst)
    }
}
