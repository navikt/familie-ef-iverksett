package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.Aktivitetskrav
import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetskravDto
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.DelvilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.FagsakdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.SøkerDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.VurderingDto


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