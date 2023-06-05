package no.nav.familie.ef.iverksett.brukernotifikasjon

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.UUID

@Service
class BrukernotifikasjonKafkaProducer(private val kafkaTemplate: KafkaTemplate<NokkelInput, BeskjedInput>) {

    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBeskjedTilBruker(iverksett: IverksettData, behandlingId: UUID) {
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

    private fun lagNøkkel(fnr: String, behandlingId: UUID): NokkelInput =
        NokkelInputBuilder()
            .withAppnavn("familie-ef-iverksett")
            .withNamespace("teamfamilie")
            .withFodselsnummer(fnr)
            .withGrupperingsId(UUID.randomUUID().toString()) // Setter random UUID uten å lagre fordi feltet skal fjernes
            .withEventId(behandlingId.toString())
            .build()

    fun lagBeskjed(iverksett: IverksettData): BeskjedInput {
        val builder = BeskjedInputBuilder()
            .withSikkerhetsnivaa(4)
            .withSynligFremTil(null)
            .withTekst(genererGOmregningMelding(iverksett))
            .withTidspunkt(LocalDateTime.now(ZoneOffset.UTC))

        return builder.build()
    }
}

fun genererGOmregningMelding(iverksett: IverksettData): String {
    val melding = if (iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.any { it.inntekt > 0 } == true) {
        gOmregningMedArbeidsinntektMelding(
            iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.first { it.periode.inneholder(YearMonth.now().plusMonths(1)) && it.inntekt > 0 }?.inntekt
                ?: iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.first { it.periode.inneholder(YearMonth.now()) && it.inntekt > 0 }?.inntekt
                ?: iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.first { it.inntekt > 0 }?.inntekt ?: throw Exception("Skulle ha funnet inntekt i andel tilkjent ytelse for behandling ${iverksett.behandling.behandlingId}"),
        )
    } else {
        G_OMREGNING_UTEN_ARBEDSINNTEKT_MELDING
    }

    return melding
}

fun gOmregningMedArbeidsinntektMelding(inntekt: Int) = """
    Fra ${nyesteGrunnbeløp.periode.fomDato.norskFormat()} har folketrygdens grunnbeløp økt til ${nyesteGrunnbeløp.grunnbeløp} kroner og din forventede inntekt er oppjustert til $inntekt kroner. Overgangsstønaden din er derfor endret.
    Du må si ifra til oss hvis inntekten din øker eller reduseres med 10 prosent eller mer.
""".trimIndent()

val G_OMREGNING_UTEN_ARBEDSINNTEKT_MELDING = """
    Fra ${nyesteGrunnbeløp.periode.fomDato.norskFormat()} har folketrygdens grunnbeløp økt til ${nyesteGrunnbeløp.grunnbeløp} og overgangsstønaden din er derfor endret.
    Du må si ifra til oss hvis månedsinntekten din blir høyere enn $halvG kroner før skatt.
""".trimIndent()
