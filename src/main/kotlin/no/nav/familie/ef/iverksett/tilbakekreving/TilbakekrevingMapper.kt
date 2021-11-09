package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandling
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype

fun Tilbakekrevingsdetaljer?.validerTilbakekreving(): Boolean {
    try {
        this?.also { lagVarsel(it) }
    } catch (e: IllegalStateException) {
        return false
    }
    return true
}

fun Iverksett.tilOpprettTilbakekrevingRequest(enhet: Enhet) =
        OpprettTilbakekrevingRequest(
                fagsystem = Fagsystem.EF,
                ytelsestype = mapYtelsestype(this.fagsak.stønadstype),
                eksternFagsakId = this.fagsak.eksternId.toString(),
                personIdent = this.søker.personIdent,
                eksternId = this.behandling.eksternId.toString(),
                behandlingstype = Behandlingstype.TILBAKEKREVING, // samme som BAKS gjør
                manueltOpprettet = false, // manuelt opprettet ennå ikke støttet i familie-tilbake?
                språkkode = Språkkode.NB, // Bør følge med iverksett.søker
                enhetId = enhet.enhetId, // iverksett.søker.tilhørendeEnhet?
                enhetsnavn = enhet.enhetNavn, // iverksett.søker.tilhørendeEnhet?
                saksbehandlerIdent = this.vedtak.saksbehandlerId,
                varsel = this.vedtak.tilbakekreving?.let { lagVarsel(it) },
                revurderingsvedtaksdato = this.vedtak.vedtaksdato,
                verge = null, // Verge er per nå ikke støttet i familie-ef-sak.
                faktainfo = lagFaktainfo(this)
        )

fun Iverksett.tilFagsystembehandling(enhet: Enhet) =
        HentFagsystemsbehandling(eksternFagsakId = this.fagsak.eksternId.toString(),
                                 eksternId = this.behandling.eksternId.toString(),
                                 ytelsestype = mapYtelsestype(this.fagsak.stønadstype),
                                 personIdent = this.søker.personIdent,
                                 språkkode = Språkkode.NB,
                                 enhetId = enhet.enhetId,
                                 enhetsnavn = enhet.enhetNavn,
                                 revurderingsvedtaksdato = this.vedtak.vedtaksdato,
                                 faktainfo = lagFaktainfo(this))

private fun mapYtelsestype(stønadType: StønadType): Ytelsestype =
        when (stønadType) {
            StønadType.BARNETILSYN -> Ytelsestype.BARNETILSYN
            StønadType.OVERGANGSSTØNAD -> Ytelsestype.OVERGANGSSTØNAD
            StønadType.SKOLEPENGER -> Ytelsestype.SKOLEPENGER
        }

private fun lagVarsel(tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer): Varsel? {
    return when (tilbakekrevingsdetaljer.tilbakekrevingsvalg) {
        Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL ->
            Varsel(tilbakekrevingsdetaljer.tilbakekrevingMedVarsel?.varseltekst
                   ?: error("varseltekst er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                   tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.sumFeilutbetaling
                   ?: error("sumFeilutbetaling er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                   tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.perioder
                   ?: error("perioder er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"))
        else -> null
    }
}

private fun lagFaktainfo(iverksett: Iverksett): Faktainfo {
    return Faktainfo(
            revurderingsårsak = iverksett.behandling.behandlingÅrsak.visningsTekst(),
            revurderingsresultat = iverksett.vedtak.vedtaksresultat.visningsnavn,
            tilbakekrevingsvalg = iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg,
            konsekvensForYtelser = emptySet() // Settes også empty av ba-sak
    )
}

private fun BehandlingÅrsak.visningsTekst(): String {
    return when(this){
        BehandlingÅrsak.SØKNAD -> "Søknad"
        BehandlingÅrsak.KLAGE -> "Klage"
        BehandlingÅrsak.NYE_OPPLYSNINGER -> "Nye opplysninger"
    }
}



