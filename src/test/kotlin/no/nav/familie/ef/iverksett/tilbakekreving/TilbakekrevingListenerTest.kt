package no.nav.familie.ef.iverksett.tilbakekreving

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.familie.ef.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.HentFagsystemsbehandling
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class TilbakekrevingListenerTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    private val tilbakekrevingProducer = mockk<TilbakekrevingProducer>()

    private lateinit var listener: TilbakekrevingListener

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.hentAvEksternId(any()) } returns opprettIverksett(behandlingId = UUID.randomUUID())
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(any()) } returns Enhet(enhetId = "0", enhetNavn = "navn")
        every { tilbakekrevingProducer.send(any()) } just runs
        listener = TilbakekrevingListener(iverksettingRepository, familieIntegrasjonerClient, tilbakekrevingProducer)
    }

    @Test
    internal fun `send kafkamelding til listener med ef-type, forvent kall til kafkaproducer`() {
        listener.listen(record(Ytelsestype.OVERGANGSSTØNAD))
        listener.listen(record(Ytelsestype.SKOLEPENGER))
        listener.listen(record(Ytelsestype.BARNETILSYN))
        verify(exactly = 3) { tilbakekrevingProducer.send(any()) }
    }

    @Test
    internal fun `send kafkamelding til listener med annen type enn overgangsstønad, forvent ingen kall til kafkaproducer`() {
        listener.listen(record(Ytelsestype.KONTANTSTØTTE))
        listener.listen(record(Ytelsestype.BARNETRYGD))
        verify(exactly = 0) { tilbakekrevingProducer.send(any()) }
    }

    private fun record(ytelsestype: Ytelsestype): ConsumerRecord<String, String> {
        val behandling = objectMapper.writeValueAsString(HentFagsystemsbehandling(eksternFagsakId = "0",
                                                                                  eksternId = "1",
                                                                                  ytelsestype = ytelsestype,
                                                                                  personIdent = "12345678910",
                                                                                  språkkode = Språkkode.NB,
                                                                                  enhetId = "enhet",
                                                                                  enhetsnavn = "enhetNavn",
                                                                                  revurderingsvedtaksdato = LocalDate.EPOCH,
                                                                                  faktainfo = Faktainfo(revurderingsårsak = "årsak",
                                                                                                        revurderingsresultat = "resultat",
                                                                                                        tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
                                                                                                        konsekvensForYtelser = emptySet())))
        return ConsumerRecord("topic", 0, 0L, "1", behandling)
    }
}