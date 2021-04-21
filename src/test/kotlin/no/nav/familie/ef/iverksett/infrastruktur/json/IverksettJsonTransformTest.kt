package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class IverksettJsonTransformTest {

    @Autowired
    var mapper : ObjectMapper? = null

    @Test
    fun `transformerVedtakJSON forvent like verdier`() {
        val json : String = ResourceLoaderTestUtil.toString(ResourceLoaderTestUtil.getResourceFrom("iverksettEksempel.json"))
        val iverksettJson: IverksettJson = mapper!!.readValue<IverksettJson>(json)

        assertThat(iverksettJson).isEqualToComparingFieldByField(iverksettJson.toDomain())
    }
}