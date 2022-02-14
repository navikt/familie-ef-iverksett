package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.OpphørÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker as BrevmottakerKontrakter
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Iverksett(
        val fagsak: Fagsakdetaljer,
        val behandling: Behandlingsdetaljer,
        val søker: Søker,
        val vedtak: Vedtaksdetaljer,
) {
    fun erMigrering(): Boolean = behandling.behandlingÅrsak == BehandlingÅrsak.MIGRERING
}

data class Fagsakdetaljer(
        val fagsakId: UUID,
        val eksternId: Long,
        val stønadstype: StønadType
)

data class Søker(
        val personIdent: String,
        val barn: List<Barn> = ArrayList(),
        val tilhørendeEnhet: String,
        val adressebeskyttelse: AdressebeskyttelseGradering? = null
)

data class Vedtaksperiode(
        val aktivitet: AktivitetType,
        val fraOgMed: LocalDate,
        val periodeType: VedtaksperiodeType,
        val tilOgMed: LocalDate
)

data class Vedtaksdetaljer(
        val vedtaksresultat: Vedtaksresultat,
        val vedtakstidspunkt: LocalDateTime,
        val opphørÅrsak: OpphørÅrsak?,
        val saksbehandlerId: String,
        val beslutterId: String,
        val tilkjentYtelse: TilkjentYtelse?,
        val vedtaksperioder: List<Vedtaksperiode>,
        val tilbakekreving: Tilbakekrevingsdetaljer? = null,
        val brevmottakere: Brevmottakere
)

data class Behandlingsdetaljer(
        val forrigeBehandlingId: UUID? = null,
        val behandlingId: UUID,
        val eksternId: Long,
        val behandlingType: BehandlingType,
        val behandlingÅrsak: BehandlingÅrsak,
        val relatertBehandlingId: UUID? = null,
        val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList(),
        val aktivitetspliktInntrefferDato: LocalDate? = null
)

data class Vilkårsvurdering(
        val vilkårType: VilkårType,
        val resultat: Vilkårsresultat,
        val delvilkårsvurderinger: List<Delvilkårsvurdering> = emptyList()
)

data class Delvilkårsvurdering(
        val resultat: Vilkårsresultat,
        val vurderinger: List<Vurdering> = emptyList()
)

data class Vurdering(
        val regelId: RegelId,
        val svar: SvarId? = null,
        val begrunnelse: String? = null
)


enum class IverksettType {
    VANLIG,
    TEKNISK_OPPHØR
}

data class Tilbakekrevingsdetaljer(
        val tilbakekrevingsvalg: Tilbakekrevingsvalg,
        val tilbakekrevingMedVarsel: TilbakekrevingMedVarsel?
)

data class TilbakekrevingMedVarsel(
        val varseltekst: String,
        val sumFeilutbetaling: BigDecimal?,
        val perioder: List<Periode>?
)

data class Brevmottakere(val mottakere: List<Brevmottaker>)
data class Brevmottaker(
    val ident: String,
    val navn: String,
    val identType: BrevmottakerKontrakter.IdentType,
    val mottakerRolle: BrevmottakerKontrakter.MottakerRolle
)
