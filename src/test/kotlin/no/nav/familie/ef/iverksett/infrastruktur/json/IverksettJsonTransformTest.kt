package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class IverksettJsonTransformTest {

    @Test
    fun `deserialiser JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettJson = objectMapper.readValue<IverksettDto>(json)
        val iverksett = iverksettJson.toDomain()
        assertThat(iverksett).isNotNull()
        assertThat(objectMapper.readTree(json))
                .isEqualTo(objectMapper.readTree(objectMapper.writeValueAsString(iverksettJson)))
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksett = objectMapper.readValue<Iverksett>(json)
        assertThat(iverksett).isNotNull
    }

    @Test
    internal fun `deserialiser iverksettOvergangsstønad til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingId)
        val parsetIverksett = objectMapper.readValue<Iverksett>(objectMapper.writeValueAsString(iverksett))
        assertThat(iverksett).isEqualTo(parsetIverksett)
    }
}