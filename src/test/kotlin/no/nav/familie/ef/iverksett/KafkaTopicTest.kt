package no.nav.familie.ef.iverksett

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class KafkaTopicTest {
    @Test
    internal fun `sjekk att navn på topic ikke finnes flere ganger`() {
        val topicsSomFinnesFlereGanger =
            getTopics().groupBy { it.isDev }
                .entries
                .map { it.value.groupBy(Topic::topic).filter { entry -> entry.value.size > 1 } }
                .flatMap { it.entries }
        if (topicsSomFinnesFlereGanger.isNotEmpty()) {
            error(lagFeilmelding(topicsSomFinnesFlereGanger))
        }
    }

    private fun lagFeilmelding(topicsDefinedMultipleTimes: List<Map.Entry<String, List<Topic>>>) =
        topicsDefinedMultipleTimes.joinToString("\n") {
            "${it.key} finnes i ${it.value.map(Topic::fullFilename)}"
        }

    private fun getTopics(): List<Topic> {
        val directories = list(Path.of("kafka-aiven"))
        return directories.flatMap { directory ->
            if (!directory.isDirectory()) error("Kan ikke ha topics på rootnivå")
            list(directory).map {
                Topic(directory.fileName.name, it.fileName.name, getTopic(it))
            }
        }.toList()
    }

    private fun list(path: Path) = Files.list(path).filter { it.name != ".DS_Store" }

    private fun getTopic(it: Path): String {
        return Files.lines(it).toList().single { it.trim().startsWith("name: ") }.trim().substringAfter(": ")
    }

    data class Topic(val directory: String, val file: String, val topic: String) {
        val isDev = file.endsWith("-dev.yaml")

        fun fullFilename() = "$directory/$file"
    }
}
