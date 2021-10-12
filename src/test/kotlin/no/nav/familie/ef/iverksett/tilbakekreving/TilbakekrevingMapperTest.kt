package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.familie.ef.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.ef.iverksett.util.opprettTilbakekrevingsdetaljer
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelse
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

internal class TilbakekrevingMapperTest {
    @Test
    fun `konverter Iverksetting til OpprettTilbakekrevingRequest`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer()
        val iverksett = opprettIverksett(behandlingsId,tilbakekreving = tilbakekreving)
        val enhet = Enhet("123","enhet")

        val request = TilbakekrevingMapper.map(iverksett,enhet)

        assertThat(request.fagsystem).isEqualTo(Fagsystem.EF)
        assertThat(request.eksternFagsakId).isEqualTo(iverksett.fagsak.eksternId.toString())
        assertThat(request.eksternId).isEqualTo(iverksett.behandling.eksternId.toString())
        assertThat(request.ytelsestype).isEqualTo(Ytelsestype.OVERGANGSSTØNAD)

        assertThat(request.enhetId).isEqualTo(enhet.enhetId)
        assertThat(request.enhetsnavn).isEqualTo(enhet.enhetNavn)

        assertThat(request.manueltOpprettet).isFalse()
        assertThat(request.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(request.behandlingstype).isEqualTo(Behandlingstype.TILBAKEKREVING)

        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving!!.tilbakekrevingsvalg)
        assertThat(request.faktainfo.revurderingsresultat).isEqualTo(iverksett.vedtak.vedtaksresultat.toString())
        assertThat(request.faktainfo.revurderingsårsak).isEqualTo(iverksett.behandling.behandlingÅrsak.toString())
        assertThat(request.faktainfo.konsekvensForYtelser).isEmpty()

        assertThat(request.revurderingsvedtaksdato).isEqualTo(iverksett.vedtak.vedtaksdato)
        assertThat(request.saksbehandlerIdent).isEqualTo(iverksett.vedtak.saksbehandlerId)

        assertThat(request.språkkode).isEqualTo(Språkkode.NB)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.sumFeilutbetaling)
        assertThat(request.varsel?.perioder).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.perioder)
    }

}