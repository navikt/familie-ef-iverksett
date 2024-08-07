package no.nav.familie.ef.iverksett.vedtakstatistikk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.ResourceLoaderTestUtil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.Barn
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.EøsUnntak
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.StønadType
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.VedtakOvergangsstønadDVH
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeOvergangsstønadDto
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat
import no.nav.familie.eksterne.kontrakter.ef.VilkårsvurderingDto
import no.nav.familie.eksterne.kontrakter.ef.ÅrsakRevurdering
import no.nav.familie.kontrakter.ef.felles.Opplysningskilde
import no.nav.familie.kontrakter.ef.felles.Revurderingsårsak
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

class VedtakstatistikkServiceTest {
    private var vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer = mockk(relaxed = true)
    private var vedtakstatistikkService = VedtakstatistikkService(vedtakstatistikkKafkaProducer)

    @Test
    internal fun `vedtakstatistikk skal kalle kafka producer med riktig data`() {
        val behandlingId = UUID.randomUUID()

        val vedtakstatistikkJsonSlot = slot<VedtakOvergangsstønadDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(vedtakstatistikkJsonSlot)) } just Runs

        val iverksettOvergangsstønad = opprettIverksettOvergangsstønad(behandlingId)
        vedtakstatistikkService.sendTilKafka(iverksettData = iverksettOvergangsstønad, forrigeIverksett = null)

        val vedtakOvergangsstønad =
            opprettVedtakstatistikkOvergangsstønad(
                behandlingId = iverksettOvergangsstønad.behandling.eksternId,
                fagsakId = iverksettOvergangsstønad.fagsak.eksternId,
                tidspunktVedtak = iverksettOvergangsstønad.vedtak.vedtakstidspunkt.toLocalDate(),
                barn = iverksettOvergangsstønad.søker.barn.map { Barn(it.personIdent, it.termindato) },
            )
        assertThat(vedtakOvergangsstønad).isEqualTo(vedtakstatistikkJsonSlot.captured)
    }

    @Test
    internal fun `map fra iverksettDtoEksempel til behandlingDVH`() {
        val iverksettDtoJson: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")

        val iverksettDto = objectMapper.readValue<IverksettDto>(iverksettDtoJson)
        val iverksett = iverksettDto.toDomain()

        val vedtakOvergangsstønadDVH = slot<VedtakOvergangsstønadDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(vedtakOvergangsstønadDVH)) } just Runs
        vedtakstatistikkService.sendTilKafka(iverksett, null)
        // val vedtakOvergangsstønadDVH = objectMapper.readValue(vedtakOvergangsstønadDVHJsonSlot.captured, VedtakOvergangsstønadDVH::class.java)
        assertThat(vedtakOvergangsstønadDVH).isNotNull
        assertThat(vedtakOvergangsstønadDVH.captured.vilkårsvurderinger.size).isEqualTo(2)
        // Egen test på vilkårtype, da det er mismatch mellom ekstern kontrakt og ef. F.eks. finnes ikke aktivitet i kontrakt.
        assertThat(
            vedtakOvergangsstønadDVH.captured.vilkårsvurderinger
                .first()
                .vilkår.name,
        ).isEqualTo(VilkårType.AKTIVITET.name)
        assertThat(
            vedtakOvergangsstønadDVH.captured.vilkårsvurderinger
                .last()
                .vilkår.name,
        ).isEqualTo(VilkårType.SAGT_OPP_ELLER_REDUSERT.name)
    }

    private fun opprettVedtakstatistikkOvergangsstønad(
        behandlingId: Long,
        fagsakId: Long,
        tidspunktVedtak: LocalDate,
        barn: List<Barn> = emptyList(),
    ): VedtakOvergangsstønadDVH =
        VedtakOvergangsstønadDVH(
            fagsakId = fagsakId,
            behandlingId = behandlingId,
            relatertBehandlingId = null,
            adressebeskyttelse = Adressebeskyttelse.UGRADERT,
            tidspunktVedtak = tidspunktVedtak.atStartOfDay(ZoneId.of("Europe/Oslo")),
            vilkårsvurderinger =
                listOf(
                    VilkårsvurderingDto(
                        vilkår = Vilkår.SAGT_OPP_ELLER_REDUSERT,
                        resultat = Vilkårsresultat.OPPFYLT,
                    ),
                ),
            person = Person(personIdent = "12345678910"),
            barn = barn,
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingÅrsak = BehandlingÅrsak.SØKNAD,
            vedtak = Vedtak.INNVILGET,
            vedtaksperioder =
                listOf(
                    VedtaksperiodeOvergangsstønadDto(
                        fraOgMed = YearMonth.now().atDay(1),
                        tilOgMed = YearMonth.now().atEndOfMonth(),
                        aktivitet = AktivitetType.BARNET_ER_SYKT,
                        periodeType = VedtaksperiodeType.HOVEDPERIODE,
                    ),
                ),
            utbetalinger =
                listOf(
                    Utbetaling(
                        beløp = 5000,
                        fraOgMed = LocalDate.parse("2021-01-01"),
                        tilOgMed = LocalDate.parse("2021-12-31"),
                        inntekt = 100,
                        inntektsreduksjon = 5,
                        samordningsfradrag = 2,
                        utbetalingsdetalj =
                            Utbetalingsdetalj(
                                klassekode = "EFOG",
                                gjelderPerson = Person(personIdent = "12345678910"),
                                delytelseId = "1",
                            ),
                    ),
                ),
            aktivitetskrav =
                Aktivitetskrav(
                    aktivitetspliktInntrefferDato = null,
                    harSagtOppArbeidsforhold = true,
                ),
            funksjonellId = 9L,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            kravMottatt = LocalDate.of(2021, 3, 3),
            årsakRevurdering =
                ÅrsakRevurdering(
                    Opplysningskilde.MELDING_MODIA.name,
                    Revurderingsårsak.ENDRING_INNTEKT.name,
                ),
            eøsUnntak = EøsUnntak(medlemMerEnn5ÅrEøs = false, medlemMerEnn5ÅrEøsAnnenForelderTrygdedekketINorge = false, oppholderSegIAnnetEøsLand = false),
        )
}
