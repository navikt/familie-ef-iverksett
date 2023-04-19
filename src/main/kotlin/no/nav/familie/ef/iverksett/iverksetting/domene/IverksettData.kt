package no.nav.familie.ef.iverksett.iverksetting.domene

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.kontrakter.ef.felles.AvslagÅrsak
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.OpphørÅrsak
import no.nav.familie.kontrakter.ef.felles.Opplysningskilde
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.Revurderingsårsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.BehandlingKategori
import no.nav.familie.kontrakter.ef.iverksett.IverksettBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettSkolepengerDto
import no.nav.familie.kontrakter.ef.iverksett.OppgaveForOpprettelseType
import no.nav.familie.kontrakter.ef.iverksett.SkolepengerStudietype
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.Datoperiode
import no.nav.familie.kontrakter.felles.Månedsperiode
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class IverksettData {

    abstract val fagsak: Fagsakdetaljer
    abstract val behandling: Behandlingsdetaljer
    abstract val søker: Søker
    abstract val vedtak: Vedtaksdetaljer

    fun erGOmregning() = behandling.behandlingÅrsak == BehandlingÅrsak.G_OMREGNING

    fun erMigrering() = behandling.behandlingÅrsak == BehandlingÅrsak.MIGRERING

    fun erKorrigeringUtenBrev() = behandling.behandlingÅrsak == BehandlingÅrsak.KORRIGERING_UTEN_BREV

    fun erSatsendring() = behandling.behandlingÅrsak == BehandlingÅrsak.SATSENDRING

    fun skalIkkeSendeBrev() = erMigrering() || erGOmregning() || erKorrigeringUtenBrev() || erSatsendring()

    abstract fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettData
}

data class IverksettOvergangsstønad(
    override val fagsak: Fagsakdetaljer,
    override val behandling: Behandlingsdetaljer,
    override val søker: Søker,
    override val vedtak: VedtaksdetaljerOvergangsstønad,
) : IverksettData() {

    override fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettOvergangsstønad {
        return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
    }
}

data class IverksettBarnetilsyn(
    override val fagsak: Fagsakdetaljer,
    override val behandling: Behandlingsdetaljer,
    override val søker: Søker,
    override val vedtak: VedtaksdetaljerBarnetilsyn,
) : IverksettData() {

    override fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettBarnetilsyn {
        return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
    }
}

data class IverksettSkolepenger(
    override val fagsak: Fagsakdetaljer,
    override val behandling: Behandlingsdetaljer,
    override val søker: Søker,
    override val vedtak: VedtaksdetaljerSkolepenger,
) : IverksettData() {

    override fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettSkolepenger {
        return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
    }
}

data class Fagsakdetaljer(
    val fagsakId: UUID,
    val eksternId: Long,
    val stønadstype: StønadType,
)

data class Søker(
    val personIdent: String,
    val barn: List<Barn> = ArrayList(),
    val tilhørendeEnhet: String,
    val adressebeskyttelse: AdressebeskyttelseGradering? = null,
)

sealed class Vedtaksperiode

data class VedtaksperiodeOvergangsstønad(
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val aktivitet: AktivitetType,
    val periodeType: VedtaksperiodeType,
) : Vedtaksperiode()

data class VedtaksperiodeBarnetilsyn(
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val utgifter: Int,
    val antallBarn: Int,
) : Vedtaksperiode()

data class VedtaksperiodeSkolepenger(
    val perioder: List<DelårsperiodeSkoleårSkolepenger> = listOf(),
    val utgiftsperioder: List<SkolepengerUtgift> = listOf(),
) : Vedtaksperiode()

data class DelårsperiodeSkoleårSkolepenger(
    val studietype: SkolepengerStudietype,
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val studiebelastning: Int,
    val makssatsForSkoleår: Int,
)

data class SkolepengerUtgift(
    val utgiftsdato: LocalDate,
    val utgifter: Int,
    val stønad: Int,
)

data class PeriodeMedBeløp(
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val beløp: Int,
)

sealed class Vedtaksdetaljer {

    abstract val vedtaksresultat: Vedtaksresultat
    abstract val vedtakstidspunkt: LocalDateTime
    abstract val opphørÅrsak: OpphørÅrsak?
    abstract val saksbehandlerId: String
    abstract val beslutterId: String
    abstract val tilkjentYtelse: TilkjentYtelse?
    abstract val tilbakekreving: Tilbakekrevingsdetaljer?
    abstract val brevmottakere: Brevmottakere?
    abstract val vedtaksperioder: List<Vedtaksperiode>
    abstract val avslagÅrsak: AvslagÅrsak?
}

data class OppgaverForOpprettelse(val oppgavetyper: List<OppgaveForOpprettelseType>)

data class VedtaksdetaljerOvergangsstønad(
    override val vedtaksresultat: Vedtaksresultat,
    override val vedtakstidspunkt: LocalDateTime,
    override val opphørÅrsak: OpphørÅrsak?,
    override val saksbehandlerId: String,
    override val beslutterId: String,
    override val tilkjentYtelse: TilkjentYtelse?,
    override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
    override val brevmottakere: Brevmottakere? = null,
    override val vedtaksperioder: List<VedtaksperiodeOvergangsstønad> = listOf(),
    override val avslagÅrsak: AvslagÅrsak? = null,
    val oppgaverForOpprettelse: OppgaverForOpprettelse = OppgaverForOpprettelse(oppgavetyper = emptyList()),
) : Vedtaksdetaljer()

data class VedtaksdetaljerBarnetilsyn(
    override val vedtaksresultat: Vedtaksresultat,
    override val vedtakstidspunkt: LocalDateTime,
    override val opphørÅrsak: OpphørÅrsak?,
    override val saksbehandlerId: String,
    override val beslutterId: String,
    override val tilkjentYtelse: TilkjentYtelse?,
    override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
    override val brevmottakere: Brevmottakere? = null,
    override val vedtaksperioder: List<VedtaksperiodeBarnetilsyn> = listOf(),
    override val avslagÅrsak: AvslagÅrsak? = null,
    val kontantstøtte: List<PeriodeMedBeløp> = listOf(),
    val tilleggsstønad: List<PeriodeMedBeløp> = listOf(),
) : Vedtaksdetaljer()

data class VedtaksdetaljerSkolepenger(
    override val vedtaksresultat: Vedtaksresultat,
    override val vedtakstidspunkt: LocalDateTime,
    override val opphørÅrsak: OpphørÅrsak?,
    override val saksbehandlerId: String,
    override val beslutterId: String,
    override val tilkjentYtelse: TilkjentYtelse?,
    override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
    override val brevmottakere: Brevmottakere? = null,
    override val vedtaksperioder: List<VedtaksperiodeSkolepenger> = listOf(),
    override val avslagÅrsak: AvslagÅrsak? = null,
    val begrunnelse: String? = null,
) : Vedtaksdetaljer()

data class Behandlingsdetaljer(
    val forrigeBehandlingId: UUID? = null,
    val behandlingId: UUID,
    val eksternId: Long,
    val behandlingType: BehandlingType,
    val behandlingÅrsak: BehandlingÅrsak,
    val relatertBehandlingId: UUID? = null,
    val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList(),
    val aktivitetspliktInntrefferDato: LocalDate? = null,
    val kravMottatt: LocalDate? = null,
    val årsakRevurdering: ÅrsakRevurdering? = null,
    val kategori: BehandlingKategori? = null,
)

data class ÅrsakRevurdering(
    val opplysningskilde: Opplysningskilde,
    val årsak: Revurderingsårsak,
)

data class Vilkårsvurdering(
    val vilkårType: VilkårType,
    val resultat: Vilkårsresultat,
    val delvilkårsvurderinger: List<Delvilkårsvurdering> = emptyList(),
)

data class Delvilkårsvurdering(
    val resultat: Vilkårsresultat,
    val vurderinger: List<Vurdering> = emptyList(),
)

data class Vurdering(
    val regelId: RegelId,
    val svar: SvarId? = null,
    val begrunnelse: String? = null,
)

data class Tilbakekrevingsdetaljer(
    val tilbakekrevingsvalg: Tilbakekrevingsvalg,
    val tilbakekrevingMedVarsel: TilbakekrevingMedVarsel?,
)

data class TilbakekrevingMedVarsel(
    val varseltekst: String,
    val sumFeilutbetaling: BigDecimal?,
    val perioder: List<Datoperiode>?,
)

private class IverksettDeserializer : StdDeserializer<IverksettData>(IverksettData::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): IverksettData {
        val mapper = p.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(p)

        val stønadstype = node.get("fagsak").get("stønadstype").asText()
        return when (StønadType.valueOf(stønadstype)) {
            StønadType.OVERGANGSSTØNAD -> mapper.treeToValue(node, IverksettOvergangsstønad::class.java)
            StønadType.BARNETILSYN -> mapper.treeToValue(node, IverksettBarnetilsyn::class.java)
            StønadType.SKOLEPENGER -> mapper.treeToValue(node, IverksettSkolepenger::class.java)
            else -> error("Har ikke mapping for $stønadstype")
        }
    }
}

class IverksettDtoDeserializer : StdDeserializer<IverksettDto>(IverksettDto::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): IverksettDto {
        val mapper = p.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(p)

        val stønadstype = node.get("fagsak").get("stønadstype").asText()
        return when (StønadType.valueOf(stønadstype)) {
            StønadType.OVERGANGSSTØNAD -> mapper.treeToValue(node, IverksettOvergangsstønadDto::class.java)
            StønadType.BARNETILSYN -> mapper.treeToValue(node, IverksettBarnetilsynDto::class.java)
            StønadType.SKOLEPENGER -> mapper.treeToValue(node, IverksettSkolepengerDto::class.java)
            else -> error("Har ikke mapping for $stønadstype")
        }
    }
}

class IverksettModule : com.fasterxml.jackson.databind.module.SimpleModule() {
    init {
        addDeserializer(IverksettDto::class.java, IverksettDtoDeserializer())
        addDeserializer(IverksettData::class.java, IverksettDeserializer())
    }
}
