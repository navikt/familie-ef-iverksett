package no.nav.familie.ef.iverksett.brukernotifikasjon

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.mockk
import no.nav.familie.ef.iverksett.lagIverksettData
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.Grunnbeløp
import no.nav.familie.kontrakter.felles.Månedsperiode
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.tms.varsel.action.Produsent
import no.nav.tms.varsel.action.Sensitivitet
import no.nav.tms.varsel.action.Tekst
import no.nav.tms.varsel.action.Varseltype
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class BrukernotifikasjonKafkaProducerTest {
    private val kafkaTemplate = mockk<KafkaTemplate<String, String>>()

    private val brukernotifikasjonKafkaProducer =
        BrukernotifikasjonKafkaProducer(
            kafkaTemplate = kafkaTemplate,
            topic = "test-topic",
            applicationName = "test-app",
            namespace = "test-namespace",
            cluster = "test-cluster",
        )

    @Nested
    inner class GenererNotifikasjonMelding {
        val forventetGOmregningTekst = "Fra 01.05.2023 har folketrygdens grunnbeløp økt til 118620 kroner og overgangsstønaden din er derfor endret. Se nav.no/minside for detaljer."
        val iverksettRevurderingInnvilget =
            lagIverksettData(
                behandlingType = BehandlingType.REVURDERING,
                vedtaksresultat = Vedtaksresultat.INNVILGET,
                andelsdatoer =
                    listOf(
                        YearMonth.now(),
                        YearMonth.now().plusMonths(1),
                    ),
                grunnbeløp =
                    Grunnbeløp(
                        periode = Månedsperiode(fom = YearMonth.of(2023, 5), tom = YearMonth.from(LocalDate.MAX)),
                        grunnbeløp = 118_620.toBigDecimal(),
                    ),
            )

        @Test
        fun `lagBeskjed genererer riktig melding`() {
            val opprettVarselJson =
                VarselActionBuilder.opprett {
                    type = Varseltype.Beskjed
                    varselId = UUID.randomUUID().toString()
                    sensitivitet = Sensitivitet.High
                    ident = "12345678910"
                    aktivFremTil = null

                    tekst =
                        Tekst(
                            spraakkode = "nb",
                            tekst = brukernotifikasjonKafkaProducer.lagMelding(iverksettRevurderingInnvilget),
                            default = true,
                        )

                    produsent =
                        Produsent(
                            cluster = "test-cluster",
                            namespace = "test-namespace",
                            appnavn = "test-app",
                        )
                }

            val opprettVarsel: Map<String, Any> = objectMapper.readValue(opprettVarselJson)
            val tekster = opprettVarsel["tekster"]

            if (tekster is List<*>) {
                val tekstMap = tekster.firstOrNull() as? Map<*, *>
                val tekst = tekstMap?.get("tekst") as? String

                Assertions
                    .assertThat(tekst)
                    .isEqualTo(forventetGOmregningTekst)
            } else {
                Assertions.fail("Tekster feltet er ikke en liste.")
            }
        }
    }
}
