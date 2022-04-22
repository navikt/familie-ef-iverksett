package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.ef.iverksett.util.opprettTilbakekrevingsdetaljer
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

internal class TilbakekrevingMapperTest {

    @Test
    fun `konverter Iverksetting til OpprettTilbakekrevingRequest`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer()
        val iverksett = opprettIverksettOvergangsstønad(behandlingsId, tilbakekreving = tilbakekreving)
        val enhet = Enhet("123", "enhet")
        val forventetRevurderingssårsak = "Søknad"

        val request = iverksett.tilOpprettTilbakekrevingRequest(enhet)

        assertThat(request.fagsystem).isEqualTo(Fagsystem.EF)
        assertThat(request.eksternFagsakId).isEqualTo(iverksett.fagsak.eksternId.toString())
        assertThat(request.eksternId).isEqualTo(iverksett.behandling.eksternId.toString())
        assertThat(request.ytelsestype).isEqualTo(Ytelsestype.OVERGANGSSTØNAD)

        assertThat(request.enhetId).isEqualTo(enhet.enhetId)
        assertThat(request.enhetsnavn).isEqualTo(ENHETSNAVN_BREV)

        assertThat(request.manueltOpprettet).isFalse
        assertThat(request.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(request.behandlingstype).isEqualTo(Behandlingstype.TILBAKEKREVING)

        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving!!.tilbakekrevingsvalg)
        assertThat(request.faktainfo.revurderingsresultat).isEqualTo(iverksett.vedtak.vedtaksresultat.visningsnavn)
        assertThat(request.faktainfo.revurderingsårsak).isEqualTo(forventetRevurderingssårsak)
        assertThat(request.faktainfo.konsekvensForYtelser).isEmpty()

        assertThat(request.revurderingsvedtaksdato).isEqualTo(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
        assertThat(request.saksbehandlerIdent).isEqualTo(iverksett.vedtak.saksbehandlerId)

        assertThat(request.språkkode).isEqualTo(Språkkode.NB)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling)
                .isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.sumFeilutbetaling)
        assertThat(request.varsel?.perioder).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.perioder)
    }

    @Test
    fun `konverter Iverksetting til Hentfagsystembehandling`() {
        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksettOvergangsstønad(behandlingsId)
        val enhet = Enhet(enhetId = "enhetId", enhetNavn = "enhetNavn")
        val fagsystemsbehandling = iverksett.tilFagsystembehandling(enhet = enhet).hentFagsystemsbehandling!!

        assertThat(fagsystemsbehandling.eksternId).isEqualTo(iverksett.behandling.eksternId.toString())
        assertThat(fagsystemsbehandling.eksternFagsakId).isEqualTo(iverksett.fagsak.eksternId.toString())
        assertThat(fagsystemsbehandling.ytelsestype.name).isEqualTo(iverksett.fagsak.stønadstype.name)
        assertThat(fagsystemsbehandling.revurderingsvedtaksdato).isEqualTo(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
        assertThat(fagsystemsbehandling.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(fagsystemsbehandling.språkkode).isEqualTo(Språkkode.NB)
        assertThat(fagsystemsbehandling.verge).isEqualTo(null)

        assertThat(fagsystemsbehandling.enhetId).isEqualTo(enhet.enhetId)
        assertThat(fagsystemsbehandling.enhetsnavn).isEqualTo(enhet.enhetNavn)
    }

    @Test
    fun `skal validere at tilbakekreving med varsel ikker gyldig uten varseltekst`() {
        val tilbakekreving = Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = null)
        assertThat(tilbakekreving.validerTilbakekreving()).isFalse
    }

    @Test
    fun `skal validere at tilbakekreving med varsel ikke er gyldig uten sumFeilutbetaling`() {
        val tilbakekreving = Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = TilbakekrevingMedVarsel(
                        varseltekst = "",
                        perioder = emptyList(),
                        sumFeilutbetaling = null
                ))
        assertThat(tilbakekreving.validerTilbakekreving()).isFalse
    }

    @Test
    fun `skal validere at tilbakekreving med varsel ikke er gyldig uten perioder`() {
        val tilbakekreving = Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                tilbakekrevingMedVarsel = TilbakekrevingMedVarsel(
                        varseltekst = "",
                        perioder = null,
                        sumFeilutbetaling = BigDecimal.ZERO
                ))
        assertThat(tilbakekreving.validerTilbakekreving()).isFalse
    }

    @Test
    fun `skal validere at tilbakekreving uten varsel ignorerer manglende varsel-data`() {
        assertThat(Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
                tilbakekrevingMedVarsel = null).validerTilbakekreving()).isTrue
        assertThat(Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING,
                tilbakekrevingMedVarsel = null).validerTilbakekreving()).isTrue
    }

    @Test
    fun `skal validere at tilbakekreving uten varsel ignorerer feil i varsel-data`() {
        val tilbakekrevingsdetaljer = Tilbakekrevingsdetaljer(
                tilbakekrevingMedVarsel = TilbakekrevingMedVarsel(
                        varseltekst = "",
                        sumFeilutbetaling = null,
                        perioder = null
                ),
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL
        )

        assertThat(tilbakekrevingsdetaljer
                           .copy(tilbakekrevingsvalg = Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING)
                           .validerTilbakekreving()).isTrue
        assertThat(tilbakekrevingsdetaljer
                           .copy(tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL)
                           .validerTilbakekreving()).isTrue
    }

}