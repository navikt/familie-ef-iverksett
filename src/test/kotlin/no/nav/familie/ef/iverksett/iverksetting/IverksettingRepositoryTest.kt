package no.nav.familie.ef.iverksett.iverksetting

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.opprettBrev
import no.nav.familie.ef.iverksett.util.opprettTekniskOpphør
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class IverksettingRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var iverksettingTestUtilRepository: IverksettingTestUtilRepository

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `Kjør script som endrer data til å inneholde vedtakstidspunkt - og sjekk at mapping fortsatt fungerer`() {
        val gammelJsonVersjonIverksett: String = ResourceLoaderTestUtil.readResource("json/IverksettVedtaksdatoEksempel.json")

        val behandlingId = UUID.randomUUID()
        iverksettingTestUtilRepository.manueltLagreIverksett(behandlingId, gammelJsonVersjonIverksett)
        val behandlingId2 = UUID.randomUUID()
        iverksettingTestUtilRepository.manueltLagreIverksett(behandlingId2, gammelJsonVersjonIverksett)
        iverksettingTestUtilRepository.oppdaterData()

        val iverksettMedVedtakstidspunkt = iverksettingRepository.hent(behandlingId)
        assertThat(iverksettMedVedtakstidspunkt).isNotNull
        assertThat(iverksettMedVedtakstidspunkt.vedtak.vedtakstidspunkt).isEqualTo(LocalDateTime.of(2021, 5, 10, 0, 0))
    }

    @Test
    fun `deserialiser og lagre iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettDtoEksempel.json")
        val iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagre(
                UUID.randomUUID(),
                iverksett,
                opprettBrev()
        )
    }

    @Test
    fun `lagre og hent iverksett, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettDtoEksempel.json")
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
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettDtoEksempel.json")
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
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        iverksettingRepository.lagreTekniskOpphør(
                iverksett.behandling.behandlingId,
                opprettTekniskOpphør(iverksett.behandling.behandlingId, iverksett.behandling.eksternId)
        )
        Assertions.assertThrows(IllegalStateException::class.java) { iverksettingRepository.hentAvEksternId(iverksett.behandling.eksternId) }
    }

    @Test
    fun `hent et sett med iverksettinger, forvent at alle blir hentet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettDtoEksempel.json")
        val iverksett: Iverksett = objectMapper.readValue<IverksettDto>(json).toDomain()
        val n = 10
        (1..n).forEach {
            iverksettingRepository.lagre(
                    UUID.randomUUID(),
                    iverksett,
                    opprettBrev()
            )
        }
        val iverksettinger = iverksettingRepository.hentAlleBehandlinger()
        assertThat(iverksettinger).hasSize(n)
    }
}