package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class IverksettJsonTransformTest {

    @Autowired
    var mapper: ObjectMapper? = null

    @Test
    fun `deserialiser JSON til IverksettJson, kall toDomain for så å kalle toJson, forvent like IverksettJson`() {
        val json: String = ResourceLoaderTestUtil.toString(ResourceLoaderTestUtil.getResourceFrom("iverksettEksempel.json"))
        val iverksettJson: IverksettJson = mapper!!.readValue<IverksettJson>(json)
        val iverksett = iverksettJson.toDomain()
        assertThat(iverksettJson).isEqualToComparingFieldByField(iverksett.toJson())
    }
}