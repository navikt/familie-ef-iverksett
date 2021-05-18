package no.nav.familie.ef.iverksett.vedtakstatistikk

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingResultat
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Inntekt
import no.nav.familie.eksterne.kontrakter.ef.Inntektstype
import no.nav.familie.eksterne.kontrakter.ef.PeriodeBeløp
import no.nav.familie.eksterne.kontrakter.ef.Periodetype
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class VedtakstatistikkServiceTest {

    private var vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer = mockk(relaxed = true)
    private var vedtakstatistikkService = VedtakstatistikkService(vedtakstatistikkKafkaProducer)

    @Test
    internal fun `vedtakstatistikk skal kalle kafka producer med riktig data`() {
        val behandlingId = UUID.randomUUID()
        val tidspunktVedtak = LocalDate.now()

        val behandlingDvhSlot = slot<BehandlingDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(behandlingDvhSlot)) } just Runs

        val iverksett = opprettIverksett(behandlingId)
        vedtakstatistikkService.sendTilKafka(iverksett = iverksett)
        verify(exactly = 1) { vedtakstatistikkKafkaProducer.sendVedtak(any()) }

        val behandlingDVH = opprettBehandlingDVH(behandlingId = behandlingId.toString(),
                                                 fagsakId = iverksett.fagsak.fagsakId.toString(),
                                                 tidspunktVedtak = iverksett.vedtak.vedtaksdato,
                                                 aktivitetspliktInntrefferDato = tidspunktVedtak)
        assertThat(behandlingDVH).isEqualTo(behandlingDvhSlot.captured)
    }

    private fun opprettBehandlingDVH(behandlingId: String,
                                     fagsakId: String,
                                     tidspunktVedtak: LocalDate,
                                     aktivitetspliktInntrefferDato: LocalDate): BehandlingDVH {
        return BehandlingDVH(fagsakId = fagsakId,
                             saksnummer = "1",
                             behandlingId = behandlingId,
                             relatertBehandlingId = null,
                             kode6eller7 = false,
                             tidspunktVedtak = tidspunktVedtak.atStartOfDay(ZoneId.of("Europe/Paris")),
                             vilkårsvurderinger = emptyList(),
                             person = Person(personIdent = "12345678910"),
                             barn = emptyList(),
                             behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                             behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                             behandlingResultat = BehandlingResultat.FERDIGSTILT,
                             vedtak = Vedtak.INNVILGET,
                             utbetalinger = listOf(Utbetaling(
                                     PeriodeBeløp(5000,
                                                  Periodetype.MÅNED,
                                                  LocalDate.parse("2021-01-01"),
                                                  LocalDate.parse("2021-12-31")),
                                     Utbetalingsdetalj(gjelderPerson = Person(personIdent = "12345678910"),
                                                       klassekode = "EFOG",
                                                       delytelseId = "11"))),
                             inntekt = listOf(Inntekt(PeriodeBeløp(150000,
                                                                   Periodetype.MÅNED,
                                                                   LocalDate.parse("2021-01-01"),
                                                                   LocalDate.parse("2021-12-31")),
                                                      Inntektstype.ARBEIDINNTEKT)),
                             aktivitetskrav = Aktivitetskrav(LocalDate.parse("2021-05-01"), false),
                             funksjonellId = "9")
    }
}