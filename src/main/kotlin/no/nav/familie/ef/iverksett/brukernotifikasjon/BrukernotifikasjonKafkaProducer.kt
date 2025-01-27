package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
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
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class BrukernotifikasjonKafkaProducer(
    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private val brukernotifikasjonTopic: String,
    @Value("\${KAFKA_TOPIC_BRUKERVARSEL}")
    private val nyBrukernotifikasjonTopic: String,
    @Value("\${NAIS_APP_NAME}")
    val applicationName: String,
    @Value("\${NAIS_NAMESPACE}")
    val namespace: String,
    @Value("\${NAIS_CLUSTER_NAME}")
    val cluster: String,
    private val kafkaTemplate: KafkaTemplate<NokkelInput, BeskjedInput>,
    private val migrertKafkaTemplate: KafkaTemplate<String, String>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBeskjedTilBruker(
        iverksett: IverksettOvergangsstønad,
        behandlingId: UUID,
    ) {
        val nokkel = lagNøkkel(iverksett.søker.personIdent, behandlingId)
        val beskjed = lagBeskjed(iverksett)

        secureLogger.info("Sender til Kafka topic: {}: {}", brukernotifikasjonTopic, beskjed)
        runCatching {
            val producerRecord = ProducerRecord(brukernotifikasjonTopic, nokkel, beskjed)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Kunne ikke sende brukernotifikasjon til topic: $brukernotifikasjonTopic. Se secure logs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende brukernotifikasjon til topic: {}", brukernotifikasjonTopic, it)
            throw RuntimeException(errorMessage)
        }
    }

    fun sendBeskjedTilBrukerMedKotlinBuilder(
        personIdent: String,
        iverksettOvergangsstønad: IverksettOvergangsstønad,
        behandlingId: UUID,
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
                        tekst = lagMelding(iverksettOvergangsstønad),
                        default = true,
                    )

                produsent =
                    Produsent(
                        cluster = cluster,
                        namespace = namespace,
                        appnavn = applicationName,
                    )
            }

        secureLogger.info("Sender til Kafka topic: {}: {}", nyBrukernotifikasjonTopic, opprettVarsel)

        runCatching {
            val producerRecord = ProducerRecord(nyBrukernotifikasjonTopic, generertVarselId, opprettVarsel)
            migrertKafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Kunne ikke sende brukernotifikasjon til topic: $nyBrukernotifikasjonTopic. Se secure logs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende brukernotifikasjon til topic: {}", nyBrukernotifikasjonTopic, it)

            throw RuntimeException(errorMessage)
        }
    }

    private fun lagNøkkel(
        fnr: String,
        behandlingId: UUID,
    ): NokkelInput =
        NokkelInputBuilder()
            .withAppnavn("familie-ef-iverksett")
            .withNamespace("teamfamilie")
            .withFodselsnummer(fnr)
            .withGrupperingsId(UUID.randomUUID().toString()) // Setter random UUID uten å lagre fordi feltet skal fjernes
            .withEventId(behandlingId.toString())
            .build()

    fun lagBeskjed(iverksett: IverksettOvergangsstønad): BeskjedInput {
        val builder =
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(4)
                .withSynligFremTil(null)
                .withTekst(lagMelding(iverksett))
                .withTidspunkt(LocalDateTime.now(ZoneOffset.UTC))

        return builder.build()
    }

    fun lagMelding(iverksett: IverksettOvergangsstønad): String =
        iverksett.vedtak.grunnbeløp?.let {
            """Fra ${it.periode.fomDato.norskFormat()} har folketrygdens grunnbeløp økt til ${it.grunnbeløp} kroner og overgangsstønaden din er derfor endret. Se nav.no/minside for detaljer.""".trimIndent()
        } ?: throw IllegalStateException("Mangler grunnbeløp")
}

private fun LocalDate.norskFormat() = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
