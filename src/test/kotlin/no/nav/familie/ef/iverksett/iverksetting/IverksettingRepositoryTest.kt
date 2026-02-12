package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.JsonMapperProvider.jsonMapper
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class IverksettingRepositoryTest : ServerTest() {
    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `lagre og hent iverksett overgangsstønad, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettData: IverksettData = jsonMapper.readValue(json, IverksettDto::class.java).toDomain()
        val iverksett = iverksettingRepository.insert(lagIverksett(iverksettData, opprettBrev()))

        val iverksettResultat = iverksettingRepository.findByIdOrThrow(iverksett.behandlingId)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett barnetilsyn, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettBarnetilsynDtoEksempel.json")
        val iverksettData: IverksettData = jsonMapper.readValue(json, IverksettDto::class.java).toDomain()
        val iverksett = iverksettingRepository.insert(lagIverksett(iverksettData, opprettBrev()))

        val iverksettResultat = iverksettingRepository.findByIdOrThrow(iverksett.behandlingId)

        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett skolepenger, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettSkolepengerDtoEksempel.json")
        val iverksettData: IverksettData = jsonMapper.readValue(json, IverksettDto::class.java).toDomain()
        val iverksett = iverksettingRepository.insert(lagIverksett(iverksettData, opprettBrev()))

        val iverksettResultat = iverksettingRepository.findByIdOrThrow(iverksett.behandlingId)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett av eksternId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettData: IverksettData = jsonMapper.readValue(json, IverksettDto::class.java).toDomain()
        val iverksett = iverksettingRepository.insert(lagIverksett(iverksettData, opprettBrev()))

        val iverksettResultat = iverksettingRepository.findByEksternId(iverksett.eksternId)

        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }
}
