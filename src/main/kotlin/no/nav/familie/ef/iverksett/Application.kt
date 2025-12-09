package no.nav.familie.ef.iverksett

import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    // TODO: Dette skal fjernes, kun for å forsikre oss om at loggin i Dev fungerer.
    val logger = LoggerFactory.getLogger(Application::class.java)
    val teamLogsMarker = MarkerFactory.getMarker("TEAM_LOGS")

    logger.info("Denne skal logge til GCP logs UTEN TeamMarker i Dev")
    logger.info(teamLogsMarker, "Denne skal logge til GCP logs MED TeamMarker i Dev")

    SpringApplication.run(Application::class.java, *args)
}
