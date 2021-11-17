package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IverksettJsonTransformTest {

    @Test
    fun `deserialiser JSON til IverksettDto, kall toDomain, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/iverksettDtoEksempel.json")
        val iverksettJson = objectMapper.readValue<IverksettDto>(json)
        val iverksett = iverksettJson.toDomain()
        assertThat(iverksett).isNotNull()
        assertThat(objectMapper.readTree(json))
                .isEqualTo(objectMapper.readTree(objectMapper.writeValueAsString(iverksettJson)))
    }


    @Test
    fun `deserialiser JSON til Iverksett, kall toDomain, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksett = objectMapper.readValue<Iverksett>(json)
        assertThat(iverksett).isNotNull
    }
}