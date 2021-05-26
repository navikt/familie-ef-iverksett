package no.nav.familie.ef.iverksett.iverksett.hent

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettDto
import no.nav.familie.ef.iverksett.infrastruktur.json.toDomain
import no.nav.familie.ef.iverksett.iverksett.lagre.LagreIverksettJdbc
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class HentIverksettJdbcTest : ServerTest() {

    @Autowired
    private lateinit var lagreIverksettJdbc: LagreIverksettJdbc

    @Autowired
    private lateinit var hentIverksettJdbc: HentIverksettJdbc

    @Test
    fun `lagre og hent iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        lagreIverksettJdbc.lagre(
            iverksett.behandling.behandlingId,
            iverksett,
            opprettBrev()
        )
        val ret = hentIverksettJdbc.hent(iverksett.behandling.behandlingId)
    }


}