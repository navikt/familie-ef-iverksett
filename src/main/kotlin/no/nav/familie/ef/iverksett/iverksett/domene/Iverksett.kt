package no.nav.familie.ef.iverksett.iverksett.domene

import no.nav.familie.kontrakter.ef.felles.BehandlingResultat
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.OpphørÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtak
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.SvarId

import java.time.LocalDate
import java.util.ArrayList
import java.util.UUID

data class Iverksett(
        val fagsak: Fagsakdetaljer,
        val behandling: Behandlingsdetaljer,
        val søker: Søker,
        val vedtak: Vedtaksdetaljer,
)

data class Fagsakdetaljer(
        val fagsakId: UUID,
        val eksternId: Long,
        val stønadstype: StønadType
)

data class Søker(
        val aktivitetskrav: Aktivitetskrav,
        val personIdent: String,
        val barn: List<Barn> = ArrayList(),
        val tilhørendeEnhet: String,
        val kode6eller7: Boolean,
)

data class Vedtaksdetaljer(
        val vedtak: Vedtak,
        val vedtaksdato: LocalDate,
        val opphørÅrsak: OpphørÅrsak?,
        val saksbehandlerId: String,
        val beslutterId: String,
        val tilkjentYtelse: TilkjentYtelse,
        val inntekter: List<Inntekt>
)

data class Behandlingsdetaljer(
        val forrigeBehandlingId: UUID? = null,
        val behandlingId: UUID,
        val eksternId: Long,
        val behandlingType: BehandlingType,
        val behandlingÅrsak: BehandlingÅrsak,
        val behandlingResultat: BehandlingResultat,
        val relatertBehandlingId: UUID? = null,
        val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList(),

        )

data class Aktivitetskrav(
        val aktivitetspliktInntrefferDato: LocalDate,
        val harSagtOppArbeidsforhold: Boolean
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


