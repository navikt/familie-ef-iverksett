package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Brevdata
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksettService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class LagreIverksettJdbcTest : ServerTest() {

    @Autowired
    private lateinit var lagreIverksettService: LagreIverksettService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `deserialiser og lagre iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksett = objectMapper.readValue<IverksettJson>(json)
        lagreIverksettService.lagreIverksettJson(
            UUID.randomUUID(),
            objectMapper.writeValueAsString(iverksett),
            listOf(Brev("1", Brevdata("mottaker", "saksbehandler", ByteArray(4096))))
        )
    }

}