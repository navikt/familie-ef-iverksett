package no.nav.familie.ef.iverksett.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZoneId
import java.time.ZonedDateTime
import no.nav.familie.kontrakter.felles.objectMapper as objectMapperFelles

object JdbcObjectMapper {

    var objectMapper = objectMapperFelles.registerModule(SimpleModule().addDeserializer(ZonedDateTime::class.java,
                                                                                        ZonedDateTimeDeserializer()))

}

/**
 * Vi ønsker å defaulte til Europe/Oslo ved deserialisering, i stedet for automatisk ZoneID-justering til UTC
 */
private class ZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime>() {

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ZonedDateTime {
        val string = jsonParser.text
        return ZonedDateTime.parse(string).withZoneSameInstant(ZoneId.of("Europe/Oslo"))
    }
}