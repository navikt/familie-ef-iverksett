package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import org.junit.jupiter.api.Test

class IverksettJsonTransformTest {

    var mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

    @Test
    fun `deserialiser JSON til IverksettJson, kall toDomain, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksettJson = mapper.readValue<IverksettDto>(json)
        val iverksett = iverksettJson.toDomain()
    }
}