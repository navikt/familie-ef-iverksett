package no.nav.familie.ef.iverksett.brukernotifikasjon

import io.mockk.mockk
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
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
    private val iverksett = mockk<IverksettData>()
    private val forventetGOmregningTekst = """
        Fra ${nyesteGrunnbeløp.periode.fomDato.norskFormat()} har folketrygdens grunnbeløp økt til ${nyesteGrunnbeløp.grunnbeløp} kroner og din forventede inntekt er oppjustert til 200000 kroner. Overgangsstønaden din er derfor endret.
        Du må si ifra til oss hvis inntekten din øker eller reduseres med 10 prosent eller mer.
    """.trimIndent()

    @Test
    fun `lagBeskjed genererer riktig melding`() {
        val iverksettRevurderingInnvilget = lagIverksettData(behandlingType = BehandlingType.REVURDERING, vedtaksresultat = Vedtaksresultat.INNVILGET, andelsdatoer = listOf(
            YearMonth.now().minusMonths(1), YearMonth.now()))
        Assertions.assertThat(brukernotifikasjonKafkaProducer.lagBeskjed(iverksettRevurderingInnvilget).getTekst()).isEqualTo(forventetGOmregningTekst)
    }
}
