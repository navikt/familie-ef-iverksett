package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.tilbakekreving.*

class TilbakekrevingMapper {

    companion object {

        fun map(iverksett: Iverksett, enhet: Enhet): OpprettTilbakekrevingRequest {
            return OpprettTilbakekrevingRequest(
                    fagsystem = Fagsystem.EF,
                    ytelsestype = mapYtelsestype(iverksett.fagsak.stønadstype),
                    eksternFagsakId = iverksett.fagsak.eksternId.toString(),
                    personIdent = iverksett.søker.personIdent,
                    eksternId = iverksett.behandling.eksternId.toString(),
                    behandlingstype = Behandlingstype.REVURDERING_TILBAKEKREVING,
                    manueltOpprettet = false,
                    språkkode = Språkkode.NB,
                    enhetId = enhet.enhetId,
                    enhetsnavn = enhet.enhetNavn,
                    saksbehandlerIdent = iverksett.vedtak.saksbehandlerId,
                    varsel = iverksett.vedtak.tilbakekreving?.let { lagVarsel(it) },
                    revurderingsvedtaksdato = iverksett.vedtak.vedtaksdato,
                    verge = null,
                    faktainfo = lagFaktainfo(iverksett)
            )
        }

        private fun mapYtelsestype(stønadType: StønadType) : Ytelsestype =
                when(stønadType) {
                    StønadType.BARNETILSYN->Ytelsestype.BARNETILSYN
                    StønadType.OVERGANGSSTØNAD->Ytelsestype.OVERGANGSSTØNAD
                    StønadType.SKOLEPENGER->Ytelsestype.SKOLEPENGER
                }

        private fun lagVarsel(tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer): Varsel? {
            return when(tilbakekrevingsdetaljer.tilbakekrevingsvalg) {
                Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL ->
                    Varsel(
                            varseltekst = tilbakekrevingsdetaljer.tilbakekrevingMedVarsel?.varseltekst ?:
                                          error("varseltekst er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                            sumFeilutbetaling = tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.sumFeilutbetaling ?:
                                                error("sumFeilutbetaling er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                            perioder = tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.perioder ?:
                                       error("perioder er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                    )
                else -> null
            }
        }

        private fun lagFaktainfo(iverksett: Iverksett): Faktainfo {
            return Faktainfo(
                    revurderingsårsak = iverksett.behandling.behandlingÅrsak.toString(),
                    revurderingsresultat = iverksett.vedtak.vedtaksresultat.toString(),  // Er dette korrekt?
                    tilbakekrevingsvalg = iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg
            )
        }

    }

}