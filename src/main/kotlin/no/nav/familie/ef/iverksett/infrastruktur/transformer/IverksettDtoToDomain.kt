package no.nav.familie.ef.iverksett.infrastruktur.transformer

import no.nav.familie.ef.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottaker
import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.Søker
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.DelvilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.FagsakdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.SøkerDto
import no.nav.familie.kontrakter.ef.iverksett.TilbakekrevingDto
import no.nav.familie.kontrakter.ef.iverksett.TilbakekrevingMedVarselDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.iverksett.VurderingDto
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker as BrevmottakerKontrakter

fun VurderingDto.toDomain(): Vurdering {
    return Vurdering(this.regelId, this.svar, this.begrunnelse)
}

fun DelvilkårsvurderingDto.toDomain(): Delvilkårsvurdering {
    return Delvilkårsvurdering(this.resultat, this.vurderinger.map { it.toDomain() })
}

fun VilkårsvurderingDto.toDomain(): Vilkårsvurdering {
    return Vilkårsvurdering(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toDomain() })
}

fun FagsakdetaljerDto.toDomain(): Fagsakdetaljer {
    return Fagsakdetaljer(fagsakId = this.fagsakId,
                          eksternId = this.eksternId,
                          stønadstype = this.stønadstype)
}

fun SøkerDto.toDomain(): Søker {
    return Søker(personIdent = this.personIdent,
                 barn = this.barn.map { it.toDomain() },
                 tilhørendeEnhet = this.tilhørendeEnhet,
                 adressebeskyttelse = this.adressebeskyttelse)
}

fun BehandlingsdetaljerDto.toDomain(): Behandlingsdetaljer {
    return Behandlingsdetaljer(behandlingId = this.behandlingId,
                               forrigeBehandlingId = this.forrigeBehandlingId,
                               eksternId = this.eksternId,
                               behandlingType = this.behandlingType,
                               behandlingÅrsak = this.behandlingÅrsak,
                               vilkårsvurderinger = this.vilkårsvurderinger.map { it.toDomain() },
                               aktivitetspliktInntrefferDato = this.aktivitetspliktInntrefferDato)
}

fun VedtaksperiodeOvergangsstønadDto.toDomain(): VedtaksperiodeOvergangsstønad {
    return VedtaksperiodeOvergangsstønad( //Dette blir generisk på et senere tidspunkt - tar det i egen PR
            aktivitet = this.aktivitet,
            fraOgMed = this.fraOgMed,
            periodeType = this.periodeType,
            tilOgMed = this.tilOgMed)
}

fun VedtaksdetaljerOvergangsstønadDto.toDomain(): VedtaksdetaljerOvergangsstønad {
    return VedtaksdetaljerOvergangsstønad(
            vedtaksresultat = this.resultat,
            vedtakstidspunkt = this.vedtakstidspunkt,
            opphørÅrsak = this.opphørÅrsak,
            saksbehandlerId = this.saksbehandlerId,
            beslutterId = this.beslutterId,
            tilkjentYtelse = this.tilkjentYtelse?.toDomain(),
            vedtaksperioder = this.vedtaksperioder.map { it.toDomain() },
            tilbakekreving = this.tilbakekreving?.toDomain(),
            brevmottakere = this.brevmottakere?.toDomain())
}

fun TilbakekrevingDto.toDomain(): Tilbakekrevingsdetaljer {
    return Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = this.tilbakekrevingsvalg,
            this.tilbakekrevingMedVarsel?.toDomain()
    )
}

fun TilbakekrevingMedVarselDto.toDomain(): TilbakekrevingMedVarsel {
    return TilbakekrevingMedVarsel(
            varseltekst = this.varseltekst,
            sumFeilutbetaling = this.sumFeilutbetaling,
            perioder = this.perioder,
    )
}

fun List<BrevmottakerKontrakter>.toDomain(): Brevmottakere {

    return Brevmottakere(mottakere = this.map {
        Brevmottaker(
                ident = it.ident,
                navn = it.navn,
                identType = it.identType,
                mottakerRolle = it.mottakerRolle
        )
    })
}

fun IverksettDto.toDomain(): Iverksett {
    return when (this) {
        is IverksettOvergangsstønadDto -> IverksettOvergangsstønad(
                fagsak = this.fagsak.toDomain(),
                søker = this.søker.toDomain(),
                behandling = this.behandling.toDomain(),
                vedtak = this.vedtak.toDomain()
        )
        else -> error("Støtter ikke mapping for ${this.javaClass.simpleName}")
    }
}
