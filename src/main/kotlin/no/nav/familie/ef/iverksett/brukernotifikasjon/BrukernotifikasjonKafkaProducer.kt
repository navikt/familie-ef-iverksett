package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
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
import no.nav.tms.varsel.action.Produsent
import no.nav.tms.varsel.action.Sensitivitet
import no.nav.tms.varsel.action.Tekst
import no.nav.tms.varsel.action.Varseltype
import no.nav.tms.varsel.builder.VarselActionBuilder

@Service
class BrukernotifikasjonKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<NokkelInput, BeskjedInput>,
    private val migrertKafkaTemplate: KafkaTemplate<String, String>,
) {
    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBeskjedTilBruker(
        iverksett: IverksettOvergangsstønad,
        behandlingId: UUID,
    ) {
        val nokkel = lagNøkkel(iverksett.søker.personIdent, behandlingId)
        val beskjed = lagBeskjed(iverksett)

        secureLogger.info("Sender til Kafka topic: {}: {}", topic, beskjed)
        runCatching {
            val producerRecord = ProducerRecord(topic, nokkel, beskjed)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Kunne ikke sende brukernotifikasjon til topic: $topic. Se secure logs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende brukernotifikasjon til topic: {}", topic, it)
            throw RuntimeException(errorMessage)
        }
    }

    fun sendBeskjedTilBrukerMedBuilder(
        personIdent: String,
        iverksettOvergangsstønad: IverksettOvergangsstønad,
        behandlingId: UUID
    ) {
        val generertVarselId = behandlingId.toString()

        val opprettVarsel = VarselActionBuilder.opprett {
            type = Varseltype.Beskjed
            varselId = generertVarselId
            sensitivitet = Sensitivitet.High
            ident = personIdent
            aktivFremTil = null

            tekst = Tekst(
                spraakkode = "nb",
                tekst = lagMelding(iverksettOvergangsstønad),
                default = true,
            )

            produsent = Produsent(
                cluster = APPLICATION_CLUSTER,
                namespace = APPLICATION_NAMESPACE,
                appnavn = APPLICATION_NAME,
            )
        }

        secureLogger.info("Sender til Kafka topic: {}: {}", topic, opprettVarsel)

        runCatching {
            val producerRecord = ProducerRecord(topic, generertVarselId, opprettVarsel)
            migrertKafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Kunne ikke sende brukernotifikasjon til topic: $topic. Se secure logs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende brukernotifikasjon til topic: {}", topic, it)

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

    companion object {
        // TODO: Disse burde kanskje ligge i Application.yml filene, men lar de være her inntil videre.
        const val APPLICATION_NAME = "familie-ef-iverksett"
        const val APPLICATION_NAMESPACE = "teamfamilie"
        const val APPLICATION_CLUSTER = "local"
    }
}

private fun LocalDate.norskFormat() = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
