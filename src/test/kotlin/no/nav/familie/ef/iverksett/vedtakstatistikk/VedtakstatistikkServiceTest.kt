package no.nav.familie.ef.iverksett.vedtakstatistikk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.StønadType
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
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

        val behandlingDvhSlot = slot<BehandlingDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(behandlingDvhSlot)) } just Runs

        val iverksett = opprettIverksett(behandlingId)
        vedtakstatistikkService.sendTilKafka(iverksett = iverksett, forrigeIverksett = null)
        verify(exactly = 1) { vedtakstatistikkKafkaProducer.sendVedtak(any()) }

        val behandlingDVH = opprettBehandlingDVH(behandlingId = iverksett.behandling.eksternId,
                                                 fagsakId = iverksett.fagsak.eksternId,
                                                 tidspunktVedtak = iverksett.vedtak.vedtakstidspunkt.toLocalDate())
        assertThat(behandlingDVH).isEqualTo(behandlingDvhSlot.captured)
    }

    @Test
    internal fun `map fra iverksettDtoEksempel til behandlingDVH`() {
        val iverksettDtoJson: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")

        val iverksettDto = objectMapper.readValue<IverksettDto>(iverksettDtoJson)
        val iverksett = iverksettDto.toDomain()

        val behandlingDvhSlot = slot<BehandlingDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(behandlingDvhSlot)) } just Runs
        vedtakstatistikkService.sendTilKafka(iverksett, null)

        assertThat(behandlingDvhSlot.captured).isNotNull
        assertThat(behandlingDvhSlot.captured.vilkårsvurderinger.size).isEqualTo(2)
        //Egen test på vilkårtype, da det er mismatch mellom ekstern kontrakt og ef. F.eks. finnes ikke aktivitet i kontrakt.
        assertThat(behandlingDvhSlot.captured.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.AKTIVITET.name)
        assertThat(behandlingDvhSlot.captured.vilkårsvurderinger.last().vilkår.name)
                .isEqualTo(VilkårType.SAGT_OPP_ELLER_REDUSERT.name)
    }

    private fun opprettBehandlingDVH(behandlingId: Long,
                                     fagsakId: Long,
                                     tidspunktVedtak: LocalDate): BehandlingDVH {
        return BehandlingDVH(fagsakId = fagsakId,
                             behandlingId = behandlingId,
                             relatertBehandlingId = null,
                             adressebeskyttelse = Adressebeskyttelse.UGRADERT,
                             tidspunktVedtak = tidspunktVedtak.atStartOfDay(ZoneId.of("Europe/Oslo")),
                             vilkårsvurderinger = listOf(VilkårsvurderingDto(vilkår = Vilkår.SAGT_OPP_ELLER_REDUSERT,
                                                                             resultat = Vilkårsresultat.OPPFYLT)),
                             person = Person(personIdent = "12345678910"),
                             barn = emptyList(),
                             behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                             behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                             vedtak = Vedtak.INNVILGET,
                             vedtaksperioder = listOf(VedtaksperiodeDto(fraOgMed = LocalDate.now(),
                                                                        tilOgMed = LocalDate.now(),
                                                                        aktivitet = AktivitetType.BARNET_ER_SYKT,
                                                                        periodeType = VedtaksperiodeType.HOVEDPERIODE)),
                             utbetalinger = listOf(Utbetaling(
                                     beløp = 5000,
                                     fraOgMed = LocalDate.parse("2021-01-01"),
                                     tilOgMed = LocalDate.parse("2021-12-31"),
                                     inntekt = 100,
                                     inntektsreduksjon = 5,
                                     samordningsfradrag = 2,
                                     utbetalingsdetalj = Utbetalingsdetalj(klassekode = "EFOG",
                                                                           gjelderPerson = Person(personIdent = "12345678910"),
                                                                           delytelseId = "1"))),

                             aktivitetskrav = Aktivitetskrav(aktivitetspliktInntrefferDato = null,
                                                             harSagtOppArbeidsforhold = true),
                             funksjonellId = 9L,
                             stønadstype = StønadType.OVERGANGSSTØNAD)
    }
}