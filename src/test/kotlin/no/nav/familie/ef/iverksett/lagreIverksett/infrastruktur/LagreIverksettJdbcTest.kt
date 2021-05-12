package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettDto
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class LagreIverksettJdbcTest : ServerTest() {

    @Autowired
    private lateinit var lagreIverksettJdbc: LagreIverksettJdbc

    @Test
    fun `deserialiser og lagre iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        lagreIverksettJdbc.lagre(
            UUID.randomUUID(),
            iverksett,
            opprettBrev()
        )
    }

}