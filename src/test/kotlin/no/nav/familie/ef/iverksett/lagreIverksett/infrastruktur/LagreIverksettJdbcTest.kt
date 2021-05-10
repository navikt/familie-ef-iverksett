package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.util.opprettBrev
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import java.util.*

class LagreIverksettJdbcTest : ServerTest() {

    @Autowired
    private lateinit var lagreIverksettJdbc: LagreIverksettJdbc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `deserialiser og lagre iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksett = objectMapper.readValue<IverksettJson>(json).toDomain()
        lagreIverksettJdbc.lagre(
            UUID.randomUUID(),
            iverksett,
            opprettBrev()
        )
    }

}