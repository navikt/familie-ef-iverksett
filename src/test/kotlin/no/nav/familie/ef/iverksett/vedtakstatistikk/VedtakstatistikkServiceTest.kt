package no.nav.familie.ef.iverksett.vedtakstatistikk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.util.toJson
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture

class VedtakstatistikkServiceTest {

    private val kafkaTemplate = mockk<KafkaTemplate<String, String>>(relaxed = true)
    private var vedtakstatistikkService = VedtakstatistikkService(kafkaTemplate, "topic")

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    internal fun `vedtakstatistikk skal kalle kafka producer med riktig data`() {
        val iverksettDtoJson: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettDto = objectMapper.readValue<IverksettDto>(iverksettDtoJson)
        val iverksett = iverksettDto.toDomain()

        val behandlingDvhSlot = slot<String>()
        val future: SettableListenableFuture<SendResult<String, String>> = SettableListenableFuture()
        every { kafkaTemplate.send(any(), any(), capture(behandlingDvhSlot)) } returns future

        vedtakstatistikkService.sendTilKafka(iverksett, null)
        val expectedVedtakstatistikk = BehandlingDVHMapper.map(iverksett, null).toJson()
        assertThat(expectedVedtakstatistikk).isEqualTo(behandlingDvhSlot.captured)
    }

    @Test
    internal fun `map fra iverksettDtoEksempel til behandlingDVH`() {
        val iverksettDtoJson: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettDto = objectMapper.readValue<IverksettDto>(iverksettDtoJson)
        val iverksett = iverksettDto.toDomain()

        val behandlingDvhSlot = slot<String>()
        val future: SettableListenableFuture<SendResult<String, String>> = SettableListenableFuture()
        every { kafkaTemplate.send(any(), any(), capture(behandlingDvhSlot)) } returns future
        vedtakstatistikkService.sendTilKafka(iverksett, null)
        val behandlingDvh = objectMapper.readValue<BehandlingDVH>(behandlingDvhSlot.captured)
        assertThat(behandlingDvh).isNotNull
        assertThat(behandlingDvh.vilkårsvurderinger.size).isEqualTo(2)
        //Egen test på vilkårtype, da det er mismatch mellom ekstern kontrakt og ef. F.eks. finnes ikke aktivitet i kontrakt.
        assertThat(behandlingDvh.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.AKTIVITET.name)
        assertThat(behandlingDvh.vilkårsvurderinger.last().vilkår.name)
                .isEqualTo(VilkårType.SAGT_OPP_ELLER_REDUSERT.name)
    }
}