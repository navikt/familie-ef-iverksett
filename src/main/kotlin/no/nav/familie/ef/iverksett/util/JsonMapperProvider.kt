package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettModule
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import java.time.ZoneId
import java.time.ZonedDateTime
import no.nav.familie.kontrakter.felles.jsonMapper as kontraktJsonMapper

object JsonMapperProvider {
    val jsonMapper: JsonMapper =
        kontraktJsonMapper
            .rebuild()
            .addModule(IverksettModule())
            .addModule(
                SimpleModule().addDeserializer(
                    ZonedDateTime::class.java,
                    ZonedDateTimeDeserializer(),
                ),
            ).build()

    /**
     * Vi ønsker å defaulte til Europe/Oslo ved deserialisering, i stedet for automatisk ZoneID-justering til UTC
     */
    private class ZonedDateTimeDeserializer : ValueDeserializer<ZonedDateTime>() {
        override fun deserialize(
            jsonParser: JsonParser,
            deserializationContext: DeserializationContext,
        ): ZonedDateTime {
            val string = jsonParser.text
            return ZonedDateTime.parse(string).withZoneSameInstant(ZoneId.of("Europe/Oslo"))
        }
    }
}
