package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IverksettJsonTransformTest {

    var mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

    @Test
    fun `deserialiser JSON til IverksettJson, kall toDomain for så å kalle toJson, forvent like IverksettJson`() {
        val json: String = ResourceLoaderTestUtil.toString(ResourceLoaderTestUtil.getResourceFrom("iverksettEksempel.json"))
        val iverksettJson: IverksettJson = mapper!!.readValue<IverksettJson>(json)
        val iverksett = iverksettJson.toDomain()
        assertThat(iverksettJson).isEqualToComparingFieldByField(iverksett.toJson())
    }
}