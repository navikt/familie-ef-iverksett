package no.nav.familie.ef.iverksett.infrastruktur.json

import no.nav.familie.ef.iverksett.iverksett.Aktivitetskrav
import no.nav.familie.ef.iverksett.iverksett.BehandlingResultat
import no.nav.familie.ef.iverksett.iverksett.BehandlingType
import no.nav.familie.ef.iverksett.iverksett.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksett.BehandlingÅrsak
import no.nav.familie.ef.iverksett.iverksett.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksett.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksett.Iverksett
import no.nav.familie.ef.iverksett.iverksett.OpphørÅrsak
import no.nav.familie.ef.iverksett.iverksett.RegelId
import no.nav.familie.ef.iverksett.iverksett.SvarId
import no.nav.familie.ef.iverksett.iverksett.Søker
import no.nav.familie.ef.iverksett.iverksett.Vedtak
import no.nav.familie.ef.iverksett.iverksett.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksett.VilkårType
import no.nav.familie.ef.iverksett.iverksett.Vilkårsresultat
import no.nav.familie.ef.iverksett.iverksett.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksett.Vurdering
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.util.*

data class IverksettDto(
        val fagsak: FagsakdetaljerDto,
        val behandling: BehandlingsdetaljerDto,
        val søker: SøkerDto,
        val vedtak: VedtaksdetaljerDto,
)

data class SøkerDto(
        val aktivitetskrav: AktivitetskravDto,
        val personIdent: String,
        val barn: List<BarnDto> = ArrayList(),
        val tilhørendeEnhet: String,
        val kode6eller7: Boolean,
)

data class FagsakdetaljerDto(
        val fagsakId: UUID,
        val eksternId: Long,
        val stønadstype: StønadType
)

data class BehandlingsdetaljerDto(
        val behandlingId: UUID,
        val forrigeBehandlingId: UUID? = null,
        val eksternId: Long,
        val behandlingType: BehandlingType,
        val behandlingÅrsak: BehandlingÅrsak,
        val behandlingResultat: BehandlingResultat,
        val relatertBehandlingId: UUID? = null,
        val vilkårsvurderinger: List<VilkårsvurderingDto> = emptyList()
)


data class VedtaksdetaljerDto(
        val vedtak: Vedtak,
        val vedtaksdato: LocalDate,
        val opphørÅrsak: OpphørÅrsak?,
        val saksbehandlerId: String,
        val beslutterId: String,
        val tilkjentYtelse: TilkjentYtelseDto,
        val inntekter: List<InntektDto>
)

data class AktivitetskravDto(
        val aktivitetspliktInntrefferDato: LocalDate,
        val harSagtOppArbeidsforhold: Boolean
)

data class VilkårsvurderingDto(
        val vilkårType: VilkårType,
        val resultat: Vilkårsresultat,
        val delvilkårsvurderinger: List<DelvilkårsvurderingDto> = emptyList()
)

data class DelvilkårsvurderingDto(
        val resultat: Vilkårsresultat,
        val vurderinger: List<VurderingDto> = emptyList()
)

data class VurderingDto(
        val regelId: RegelId,
        val svar: SvarId? = null,
        val begrunnelse: String? = null
)

fun VurderingDto.toDomain(): Vurdering {
    return Vurdering(this.regelId, this.svar, this.begrunnelse)
}

fun DelvilkårsvurderingDto.toDomain(): Delvilkårsvurdering {
    return Delvilkårsvurdering(this.resultat, this.vurderinger.map { it.toDomain() })
}

fun VilkårsvurderingDto.toDomain(): Vilkårsvurdering {
    return Vilkårsvurdering(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toDomain() })
}

fun AktivitetskravDto.toDomain(): Aktivitetskrav {
    return Aktivitetskrav(this.aktivitetspliktInntrefferDato, this.harSagtOppArbeidsforhold)
}

fun FagsakdetaljerDto.toDomain(): Fagsakdetaljer {
    return Fagsakdetaljer(fagsakId = this.fagsakId,
                          eksternId = this.eksternId,
                          stønadstype = this.stønadstype)
}

fun SøkerDto.toDomain(): Søker {
    return Søker(aktivitetskrav = this.aktivitetskrav.toDomain(),
                 personIdent = this.personIdent,
                 barn = this.barn.map { it.toDomain()},
                 tilhørendeEnhet = this.tilhørendeEnhet,
                 kode6eller7 = this.kode6eller7)
}

fun BehandlingsdetaljerDto.toDomain(): Behandlingsdetaljer {
    return Behandlingsdetaljer(behandlingId = this.behandlingId,
                               forrigeBehandlingId = this.forrigeBehandlingId,
                               eksternId = this.eksternId,
                               behandlingType = this.behandlingType,
                               behandlingÅrsak = this.behandlingÅrsak,
                               behandlingResultat = this.behandlingResultat,
                               relatertBehandlingId = this.relatertBehandlingId,
                               vilkårsvurderinger = this.vilkårsvurderinger.map { it.toDomain() })
}

fun VedtaksdetaljerDto.toDomain(): Vedtaksdetaljer {
    return Vedtaksdetaljer(vedtak = this.vedtak,
                           vedtaksdato = this.vedtaksdato,
                           opphørÅrsak = this.opphørÅrsak,
                           saksbehandlerId = this.saksbehandlerId,
                           beslutterId = this.beslutterId,
                           tilkjentYtelse = this.tilkjentYtelse.toDomain(),
                           inntekter = this.inntekter.map { it.toDomain() })
}

fun IverksettDto.toDomain(): Iverksett {
    return Iverksett(
            fagsak = this.fagsak.toDomain(),
            søker = this.søker.toDomain(),
            behandling = this.behandling.toDomain(),
            vedtak = this.vedtak.toDomain()
    )
}
