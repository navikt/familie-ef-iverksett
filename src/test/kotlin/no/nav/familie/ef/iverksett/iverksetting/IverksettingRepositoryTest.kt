package no.nav.familie.ef.iverksett.iverksetting

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettTekniskOpphør
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class IverksettingRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `lagre og hent iverksett overgangsstønad, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagre(
                iverksett.behandling.behandlingId,
                iverksett,
                opprettBrev()
        )
        val iverksettResultat = iverksettingRepository.hent(iverksett.behandling.behandlingId)
        assertThat(iverksett).isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett barnetilsyn, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettBarnetilsynDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagre(
                iverksett.behandling.behandlingId,
                iverksett,
                opprettBrev()
        )
        val iverksettResultat = iverksettingRepository.hent(iverksett.behandling.behandlingId)
        assertThat(iverksett).isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett av eksternId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagre(
                iverksett.behandling.behandlingId,
                iverksett,
                opprettBrev()
        )
        val iverksettResultat = iverksettingRepository.hentAvEksternId(iverksett.behandling.eksternId)
        assertThat(iverksett).isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent teknisk opphør av eksternId, forvent IllegalStateException`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagreTekniskOpphør(
                iverksett.behandling.behandlingId,
                opprettTekniskOpphør(iverksett.behandling.behandlingId, iverksett.behandling.eksternId)
        )
        Assertions.assertThrows(IllegalStateException::class.java) { iverksettingRepository.hentAvEksternId(iverksett.behandling.eksternId) }
    }

}