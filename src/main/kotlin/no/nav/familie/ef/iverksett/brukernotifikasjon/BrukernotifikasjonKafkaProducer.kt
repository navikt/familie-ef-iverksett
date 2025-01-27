package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.tms.varsel.action.Produsent
import no.nav.tms.varsel.action.Sensitivitet
import no.nav.tms.varsel.action.Tekst
import no.nav.tms.varsel.action.Varseltype
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class BrukernotifikasjonKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${BRUKERNOTIFIKASJON_BESKJED_TOPIC}")
    private val topic: String,
    @Value("\${NAIS_APP_NAME}")
    val applicationName: String,
    @Value("\${NAIS_NAMESPACE}")
    val namespace: String,
    @Value("\${NAIS_CLUSTER_NAME}")
    val cluster: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBeskjedTilBruker(
        personIdent: String,
        iverksettOvergangsstønad: IverksettOvergangsstønad,
        behandlingId: UUID,
        melding: String,
    ) {
        val generertVarselId = behandlingId.toString()

        val opprettVarsel =
            VarselActionBuilder.opprett {
                type = Varseltype.Beskjed
                varselId = generertVarselId
                sensitivitet = Sensitivitet.High
                ident = personIdent
                aktivFremTil = null

                tekst =
                    Tekst(
                        spraakkode = "nb",
                        tekst = melding,
                        default = true,
                    )

                produsent =
                    Produsent(
                        cluster = cluster,
                        namespace = namespace,
                        appnavn = applicationName,
                    )
            }

        secureLogger.info("Sender til Kafka topic: {}: {}", topic, opprettVarsel)

        runCatching {
            val producerRecord = ProducerRecord(topic, generertVarselId, opprettVarsel)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Kunne ikke sende brukernotifikasjon til topic: $topic. Se secure logs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende brukernotifikasjon til topic: {}", topic, it)

            throw RuntimeException(errorMessage)
        }
    }

    fun lagMelding(iverksett: IverksettOvergangsstønad): String =
        iverksett.vedtak.grunnbeløp?.let {
            """Fra ${it.periode.fomDato.norskFormat()} har folketrygdens grunnbeløp økt til ${it.grunnbeløp} kroner og overgangsstønaden din er derfor endret. Se nav.no/minside for detaljer.""".trimIndent()
        } ?: throw IllegalStateException("Mangler grunnbeløp")
}

private fun LocalDate.norskFormat() = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
