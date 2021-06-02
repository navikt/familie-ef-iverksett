package no.nav.familie.ef.iverksett.iverksetting

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class IverksettingRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `deserialiser og lagre iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagre(
            UUID.randomUUID(),
            iverksett,
            opprettBrev()
        )
    }

    @Test
    fun `lagre og hent iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagre(
                iverksett.behandling.behandlingId,
                iverksett,
                opprettBrev()
        )
        val ret = iverksettingRepository.hent(iverksett.behandling.behandlingId)
        // TODO: Sjekk innholdet?
    }
}