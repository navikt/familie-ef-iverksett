package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.util.JsonMapperProvider.jsonMapper
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.kontrakter.ef.iverksett.IverksettBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettSkolepengerDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import tools.jackson.databind.node.ObjectNode
import java.util.UUID

/**
 * Dersom testene i denne filen feiler i maven-bygg, men ikke når det kjøres i IntelliJ,
 * så hjelper det sannsynligvis å reloade maven dependencies.
 */
class IverksettJsonTransformTest {
    @Test
    fun `deserialiser overgangsstønad JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettJson = jsonMapper.readValue(json, IverksettDto::class.java)
        val iverksett = iverksettJson.toDomain()

        assertThat(iverksettJson).isInstanceOf(IverksettOvergangsstønadDto::class.java)
        assertThat(iverksett).isInstanceOf(IverksettOvergangsstønad::class.java)

        assertThat(iverksett).isNotNull
        // Verifiser at roundtrip fungerer ved å sammenligne objektene
        val roundTrippedJson = jsonMapper.readValue(jsonMapper.writeValueAsString(iverksettJson), IverksettDto::class.java)
        assertThat(roundTrippedJson).isEqualTo(iverksettJson)
    }

    @Test
    fun `deserialiser barnetilsyn JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettBarnetilsynDtoEksempel.json")
        val iverksettJson = jsonMapper.readValue(json, IverksettDto::class.java)
        val iverksett = iverksettJson.toDomain()

        assertThat(iverksettJson).isInstanceOf(IverksettBarnetilsynDto::class.java)
        assertThat(iverksett).isInstanceOf(IverksettBarnetilsyn::class.java)

        assertThat(iverksett).isNotNull
        // Verifiser at roundtrip fungerer ved å sammenligne objektene
        val roundTrippedJson = jsonMapper.readValue(jsonMapper.writeValueAsString(iverksettJson), IverksettDto::class.java)
        assertThat(roundTrippedJson).isEqualTo(iverksettJson)
    }

    @Test
    fun `deserialiser skolepenger JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettSkolepengerDtoEksempel.json")
        val iverksettJson = jsonMapper.readValue(json, IverksettDto::class.java)
        val iverksett = iverksettJson.toDomain()

        assertThat(iverksettJson).isInstanceOf(IverksettSkolepengerDto::class.java)
        assertThat(iverksett).isInstanceOf(IverksettSkolepenger::class.java)

        assertThat(iverksett).isNotNull
        // Verifiser at roundtrip fungerer ved å sammenligne objektene
        val roundTrippedJson = jsonMapper.readValue(jsonMapper.writeValueAsString(iverksettJson), IverksettDto::class.java)
        assertThat(roundTrippedJson).isEqualTo(iverksettJson)
    }

    @Test
    fun `deserialiser overgangsstønad JSON med feil stønadtype`() {
        val stønadType = StønadType.BARNETILSYN
        val filnavn = "json/IverksettDtoEksempel.json"
        parseJsonMedFeilStønadstype(filnavn, stønadType)
    }

    @Test
    fun `deserialiser barnetilsyn JSON med feil stønadtype`() {
        val stønadType = StønadType.OVERGANGSSTØNAD
        val filnavn = "json/IverksettBarnetilsynDtoEksempel.json"
        parseJsonMedFeilStønadstype(filnavn, stønadType)
    }

    @Test
    fun `deserialiser skolepenger JSON med feil stønadtype`() {
        val stønadType = StønadType.OVERGANGSSTØNAD
        val filnavn = "json/IverksettSkolepengerDtoEksempel.json"
        parseJsonMedFeilStønadstype(filnavn, stønadType)
    }

    private fun parseJsonMedFeilStønadstype(
        filnavn: String,
        stønadType: StønadType,
    ) {
        val json: String = ResourceLoaderTestUtil.readResource(filnavn)
        val tree = jsonMapper.readTree(json)
        (tree.get("fagsak") as ObjectNode).put("stønadstype", stønadType.name)
        val jsonMedFeilStønadstype = jsonMapper.writeValueAsString(tree)
        assertThatThrownBy { jsonMapper.readValue(jsonMedFeilStønadstype, IverksettDto::class.java) }
            .isInstanceOf(tools.jackson.databind.DatabindException::class.java)
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksett = jsonMapper.readValue(json, IverksettData::class.java)
        assertThat(iverksett).isNotNull
    }

    @Test
    internal fun `deserialiser iverksettOvergangsstønad til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = opprettIverksettOvergangsstønad(behandlingId)
        val parsetIverksett = jsonMapper.readValue(jsonMapper.writeValueAsString(iverksett), IverksettData::class.java)
        assertThat(iverksett).isEqualTo(parsetIverksett)
    }
}
