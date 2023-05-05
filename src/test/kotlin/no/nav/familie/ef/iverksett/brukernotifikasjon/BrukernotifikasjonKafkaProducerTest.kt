package no.nav.familie.ef.iverksett.brukernotifikasjon

import io.mockk.mockk
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.YearMonth

class BrukernotifikasjonKafkaProducerTest {

    private val kafkaTemplate = mockk<KafkaTemplate<NokkelInput, BeskjedInput>>()
    private val brukernotifikasjonKafkaProducer = BrukernotifikasjonKafkaProducer(kafkaTemplate)

    private val forventetGOmregningTekst = """
        Folketrygdens grunnbeløp er økt til <Nytt G-beløp> kroner pr. år fra 1. mai ${YearMonth.now().year}. Ytelsen er derfor omregnet.
    """.trimIndent()

    @Test
    fun `lagBeskjed genererer riktig melding`() {
        Assertions.assertThat(brukernotifikasjonKafkaProducer.lagBeskjed().getTekst()).isEqualTo(forventetGOmregningTekst)
    }
}
