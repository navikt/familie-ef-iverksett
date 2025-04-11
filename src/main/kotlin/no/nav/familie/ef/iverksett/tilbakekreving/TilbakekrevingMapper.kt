package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.iverksett.BehandlingKategori
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Regelverk
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Brevmottaker
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandling
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandlingRespons
import no.nav.familie.kontrakter.felles.tilbakekreving.MottakerType
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype

const val ENHETSNAVN_BREV = "NAV Arbeid og ytelser"

fun Tilbakekrevingsdetaljer?.validerTilbakekreving(): Boolean {
    try {
        this?.also { lagVarsel(it) }
    } catch (e: IllegalStateException) {
        return false
    }
    return true
}

fun IverksettData.tilOpprettTilbakekrevingRequest(enhet: Enhet) =
    OpprettTilbakekrevingRequest(
        fagsystem = Fagsystem.EF,
        ytelsestype = Ytelsestype.valueOf(this.fagsak.stønadstype.name),
        eksternFagsakId = this.fagsak.eksternId.toString(),
        personIdent = this.søker.personIdent,
        eksternId = this.behandling.eksternId.toString(),
        behandlingstype = Behandlingstype.TILBAKEKREVING, // samme som BAKS gjør
        manueltOpprettet = false, // manuelt opprettet ennå ikke støttet i familie-tilbake?
        språkkode = Språkkode.NB, // Bør følge med iverksett.søker
        enhetId = enhet.enhetId, // iverksett.søker.tilhørendeEnhet?
        enhetsnavn = ENHETSNAVN_BREV, // Det som kommer etter "Med vennlig hilsen" i tilbakekrevingsbrev.
        saksbehandlerIdent = this.vedtak.saksbehandlerId,
        varsel = this.vedtak.tilbakekreving?.let { lagVarsel(it) },
        revurderingsvedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
        verge = null, // Verge er per nå ikke støttet i familie-ef-sak.
        faktainfo = lagFaktainfo(this),
        regelverk = tilRegelverk(this.behandling.kategori),
        begrunnelseForTilbakekreving = this.vedtak.tilbakekreving?.begrunnelseForTilbakekreving,
        manuelleBrevmottakere = tilManuelleBrevmottakere(this.vedtak.brevmottakere?.mottakere),
    )

fun tilManuelleBrevmottakere(brevmottakere: List<no.nav.familie.ef.iverksett.brev.domain.Brevmottaker>?): Set<Brevmottaker> {
    val manuelleBrevmottakere =
        brevmottakere
            ?.filter { it.mottakerRolle != no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.BRUKER }
            ?.map {
                Brevmottaker(
                    type =
                        when (it.mottakerRolle) {
                            no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.FULLMEKTIG -> MottakerType.FULLMEKTIG
                            no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.MottakerRolle.VERGE -> MottakerType.VERGE
                            else -> {
                                throw IllegalStateException("Skulle hatt mottaker-rolle som er enten verge eller fullmektig, men var: ${it.mottakerRolle}")
                            }
                        },
                    navn = it.navn,
                    personIdent = it.ident,
                    vergetype =
                        when {
                            it.identType == no.nav.familie.kontrakter.ef.iverksett.Brevmottaker.IdentType.ORGANISASJONSNUMMER -> Vergetype.ADVOKAT // Brukes her for generelt mottaker som er organisasjon, tilbakekreving behandler advokat som en organisasjon
                            else -> Vergetype.UDEFINERT
                        },
                )
            }
    return manuelleBrevmottakere?.toSet() ?: emptySet()
}

fun IverksettData.tilFagsystembehandling(enhet: Enhet) =
    HentFagsystemsbehandlingRespons(
        hentFagsystemsbehandling =
            HentFagsystemsbehandling(
                eksternFagsakId = this.fagsak.eksternId.toString(),
                eksternId = this.behandling.eksternId.toString(),
                ytelsestype = Ytelsestype.valueOf(this.fagsak.stønadstype.name),
                personIdent = this.søker.personIdent,
                språkkode = Språkkode.NB,
                enhetId = enhet.enhetId,
                enhetsnavn = enhet.enhetNavn,
                revurderingsvedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
                faktainfo = lagFaktainfo(this),
            ),
    )

private fun lagVarsel(tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer): Varsel? =
    when (tilbakekrevingsdetaljer.tilbakekrevingsvalg) {
        Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL ->
            Varsel(
                tilbakekrevingsdetaljer.tilbakekrevingMedVarsel?.varseltekst
                    ?: error("varseltekst er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.sumFeilutbetaling
                    ?: error("sumFeilutbetaling er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.perioder?.map { Periode(it.fom, it.tom) }
                    ?: error("perioder er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
            )

        else -> null
    }

private fun lagFaktainfo(iverksett: IverksettData): Faktainfo =
    Faktainfo(
        revurderingsårsak = iverksett.behandling.behandlingÅrsak.visningsTekst(),
        revurderingsresultat = iverksett.vedtak.vedtaksresultat.visningsnavn,
        tilbakekrevingsvalg = iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg,
        konsekvensForYtelser = emptySet(), // Settes også empty av ba-sak
    )

private fun BehandlingÅrsak.visningsTekst(): String =
    when (this) {
        BehandlingÅrsak.SØKNAD -> "Søknad"
        BehandlingÅrsak.KLAGE -> "Klage"
        BehandlingÅrsak.NYE_OPPLYSNINGER -> "Nye opplysninger"
        BehandlingÅrsak.KORRIGERING_UTEN_BREV -> "Korrigering uten brev"
        BehandlingÅrsak.IVERKSETTE_KA_VEDTAK -> "Iverksette KA-vedtak (uten brev)"
        BehandlingÅrsak.PAPIRSØKNAD -> "Papirsøknad"
        BehandlingÅrsak.MANUELT_OPPRETTET -> "Manuelt opprettet"
        BehandlingÅrsak.G_OMREGNING -> "G-omregning"
        BehandlingÅrsak.SATSENDRING -> "Satsendring"
        BehandlingÅrsak.AUTOMATISK_INNTEKTSENDRING -> "Automatisk inntektsendring"
        BehandlingÅrsak.MIGRERING,
        BehandlingÅrsak.SANKSJON_1_MND,
        -> error("Skal ikke gi tilbakekreving for årsak=$this")
    }

private fun tilRegelverk(kategori: BehandlingKategori?) =
    when (kategori) {
        BehandlingKategori.EØS -> Regelverk.EØS
        BehandlingKategori.NASJONAL -> Regelverk.NASJONAL
        null -> Regelverk.NASJONAL
    }
