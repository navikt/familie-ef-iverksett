package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.domene.BehandlingResultat
import no.nav.familie.ef.iverksett.domene.BehandlingType
import no.nav.familie.ef.iverksett.domene.BehandlingÅrsak
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.OpphørÅrsak
import no.nav.familie.ef.iverksett.domene.Periodetype
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseStatus
import no.nav.familie.ef.iverksett.domene.Vedtak
import no.nav.familie.ef.iverksett.infrastruktur.json.AktivitetskravDto
import no.nav.familie.ef.iverksett.infrastruktur.json.AndelTilkjentYtelseDto
import no.nav.familie.ef.iverksett.infrastruktur.json.BehandlingsdetaljerDto
import no.nav.familie.ef.iverksett.infrastruktur.json.FagsakdetaljerDto
import no.nav.familie.ef.iverksett.infrastruktur.json.InntektDto
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettDto
import no.nav.familie.ef.iverksett.infrastruktur.json.PeriodebeløpDto
import no.nav.familie.ef.iverksett.infrastruktur.json.SøkerDto
import no.nav.familie.ef.iverksett.infrastruktur.json.TilkjentYtelseDto
import no.nav.familie.ef.iverksett.infrastruktur.json.VedtaksdetaljerDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.util.UUID

fun opprettIverksettDto(behandlingId: UUID): IverksettDto {

    val inntekt = InntektDto(periodebeløp = PeriodebeløpDto(utbetaltPerPeriode = 150000,
                                                            periodetype = Periodetype.MÅNED,
                                                            fraOgMed = LocalDate.of(2021, 1, 1),
                                                            tilOgMed = LocalDate.of(2021, 12, 31)), inntektstype = null)

    val andelTilkjentYtelse = AndelTilkjentYtelseDto(periodebeløp = PeriodebeløpDto(utbetaltPerPeriode = 5000,
                                                                                    periodetype = Periodetype.MÅNED,
                                                                                    fraOgMed = LocalDate.of(2021, 1, 1),
                                                                                    tilOgMed = LocalDate.of(2021, 12, 31)),
                                                     periodeId = 1,
                                                     forrigePeriodeId = null,
                                                     kildeBehandlingId = UUID.randomUUID())
    val tilkjentYtelse = TilkjentYtelseDto(
            andelerTilkjentYtelse = listOf(andelTilkjentYtelse)
    )

    return IverksettDto(
            fagsak = FagsakdetaljerDto(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
            behandling = BehandlingsdetaljerDto(behandlingId = behandlingId,
                                                forrigeBehandlingId = null,
                                                eksternId = 9L,
                                                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                                                behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                                                behandlingResultat = BehandlingResultat.FERDIGSTILT,
                                                relatertBehandlingId = null,
                                                vilkårsvurderinger = emptyList()),
            søker = SøkerDto(aktivitetskrav = AktivitetskravDto(
                    aktivitetspliktInntrefferDato = LocalDate.of(2021, 5, 1),
                    harSagtOppArbeidsforhold = false),
                             personIdent = "12345678910",
                             barn = emptyList(),
                             tilhørendeEnhet = "4489",
                             kode6eller7 = false),
            vedtak = VedtaksdetaljerDto(vedtak = Vedtak.INNVILGET,
                                        vedtaksdato = LocalDate.of(2021, 5, 12),
                                        opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                                        saksbehandlerId = "A12345",
                                        beslutterId = "B23456",
                                        tilkjentYtelse = tilkjentYtelse,
                                        inntekter = listOf(inntekt))
    )
}

fun opprettBrev(): Brev {
    return Brev("234bed7c-b1d3-11eb-8529-0242ac130003", ByteArray(256))
}