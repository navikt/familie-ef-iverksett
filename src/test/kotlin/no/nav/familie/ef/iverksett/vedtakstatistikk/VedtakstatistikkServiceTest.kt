package no.nav.familie.ef.iverksett.vedtakstatistikk

import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.util.opprettIverksettJson
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingResultat
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class VedtakstatistikkServiceTest {

    private var vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer = mockk(relaxed = true)
    private var vedtakstatistikkService = VedtakstatistikkService(vedtakstatistikkKafkaProducer)

    @Test
    internal fun `vedtakstatistikk skal kalle kafka producer med riktig data`() {
        val behandlingId = "123"
        val tidspunktVedtak = LocalDate.now()

        val iverksettJson = opprettIverksettJson(behandlingId, emptyList(), tidspunktVedtak, tidspunktVedtak)
        val behandlingDVH = opprettBehandlingDVH(behandlingId = behandlingId,
                                                 tidspunktVedtak = tidspunktVedtak,
                                                 aktivitetspliktInntrefferDato = tidspunktVedtak)
        vedtakstatistikkService.sendTilKafka(iverksettJson = iverksettJson)
        verify(exactly = 1) { vedtakstatistikkKafkaProducer.sendVedtak(behandlingDVH) }
    }

    private fun opprettBehandlingDVH(behandlingId: String,
                                     tidspunktVedtak: LocalDate,
                                     aktivitetspliktInntrefferDato: LocalDate): BehandlingDVH {
        return BehandlingDVH(fagsakId = "1",
                             saksnummer = "1",
                             behandlingId = behandlingId,
                             relatertBehandlingId = "2",
                             kode6eller7 = false,
                             tidspunktVedtak = tidspunktVedtak.atStartOfDay(ZoneId.of("Europe/Paris")),
                             vilkårsvurderinger = emptyList(),
                             person = Person(personIdent = "12345678910"),
                             barn = emptyList(),
                             behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                             behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                             behandlingResultat = BehandlingResultat.FERDIGSTILT,
                             vedtak = Vedtak.INNVILGET,
                             utbetalinger = emptyList(),
                             inntekt = emptyList(),
                             aktivitetskrav = Aktivitetskrav(aktivitetspliktInntrefferDato, false),
                             funksjonellId = "0")
    }
}