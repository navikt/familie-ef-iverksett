package no.nav.familie.ef.iverksett.tilbakekreving

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandling
import no.nav.familie.kontrakter.felles.tilbakekreving.FeilutbetaltePerioderDto
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

internal class TilbakekrevingClientTest : ServerTest() {

    @Autowired
    private lateinit var tilbakekrevingClient: TilbakekrevingClient

    @Test
    fun `hentForhåndsvisningVarselbrev returnere en byteArray med data fra server`() {
        val forhåndsvisVarselbrevRequest =
            ForhåndsvisVarselbrevRequest(
                ytelsestype = Ytelsestype.OVERGANGSSTØNAD,
                behandlendeEnhetsNavn = "Oslo",
                språkkode = Språkkode.NB,
                feilutbetaltePerioderDto = FeilutbetaltePerioderDto(
                    654654L,
                    listOf(
                        Periode(
                            LocalDate.MIN,
                            LocalDate.MAX,
                        ),
                    ),
                ),
                fagsystem = Fagsystem.EF,
                ident = "32165498721",
                eksternFagsakId = "654654",
            )
        val hentForhåndsvisningVarselbrev = tilbakekrevingClient.hentForhåndsvisningVarselbrev(forhåndsvisVarselbrevRequest)

        assertThat(hentForhåndsvisningVarselbrev.decodeToString()).isEqualTo("Dette er en PDF!")
    }

    @Test
    fun `finnesÅpenBehandling returnerer true hvis server retrurnerer transportobjekt med true`() {
        val finnesÅpenBehandling = tilbakekrevingClient.finnesÅpenBehandling(1L)

        assertThat(finnesÅpenBehandling).isEqualTo(true)
    }

    @Test
    fun `finnBehandlinger returnerer enn liste med behandlinger`() {
        val finnBehandlinger = tilbakekrevingClient.finnBehandlinger(1L)

        assertThat(finnBehandlinger).hasSize(1)
        assertThat(finnBehandlinger.first()).isInstanceOf(Behandling::class.java)
    }

    @Test
    fun `kanBehandlingOpprettesManuelt returnere transportobjekt far server`() {
        val kanBehandlingOpprettesManuelt =
            tilbakekrevingClient.kanBehandlingOpprettesManuelt(1L, Ytelsestype.OVERGANGSSTØNAD)

        assertThat(kanBehandlingOpprettesManuelt.kanBehandlingOpprettes).isEqualTo(true)
    }
}
