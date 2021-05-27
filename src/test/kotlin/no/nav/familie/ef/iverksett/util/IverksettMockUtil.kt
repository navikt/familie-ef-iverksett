package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.iverksett.domene.Aktivitetskrav
import no.nav.familie.ef.iverksett.iverksett.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksett.domene.Behandlingsdetaljer
import no.nav.familie.ef.iverksett.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.iverksett.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksett.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksett.domene.Inntekt
import no.nav.familie.ef.iverksett.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksett.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksett.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksett.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.iverksett.domene.Søker
import no.nav.familie.ef.iverksett.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksett.domene.Vedtaksdetaljer
import no.nav.familie.kontrakter.ef.felles.BehandlingResultat
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.OpphørÅrsak
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.TilkjentYtelseStatus
import no.nav.familie.kontrakter.ef.felles.Vedtak
import no.nav.familie.kontrakter.ef.iverksett.AktivitetskravDto
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.BehandlingsdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.FagsakdetaljerDto
import no.nav.familie.kontrakter.ef.iverksett.InntektDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.PeriodebeløpDto
import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import no.nav.familie.kontrakter.ef.iverksett.SøkerDto
import no.nav.familie.kontrakter.ef.iverksett.TilkjentYtelseDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksdetaljerDto
import java.time.LocalDate
import java.util.UUID

fun opprettIverksettDto(behandlingId: UUID): IverksettDto {

    val inntekt = InntektDto(
            periodebeløp = PeriodebeløpDto(
                    beløp = 150000,
                    periodetype = Periodetype.MÅNED,
                    fraOgMed = LocalDate.of(2021, 1, 1),
                    tilOgMed = LocalDate.of(2021, 12, 31)
            ), inntektstype = null
    )

    val andelTilkjentYtelse = AndelTilkjentYtelseDto(
            periodebeløp = PeriodebeløpDto(
                    beløp = 5000,
                    periodetype = Periodetype.MÅNED,
                    fraOgMed = LocalDate.of(2021, 1, 1),
                    tilOgMed = LocalDate.of(2021, 12, 31)
            ),
            kildeBehandlingId = UUID.randomUUID()
    )
    val tilkjentYtelse = TilkjentYtelseDto(
            andelerTilkjentYtelse = listOf(andelTilkjentYtelse)
    )

    return IverksettDto(
            fagsak = FagsakdetaljerDto(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
            behandling = BehandlingsdetaljerDto(
                    behandlingId = behandlingId,
                    forrigeBehandlingId = null,
                    eksternId = 9L,
                    behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                    behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                    behandlingResultat = BehandlingResultat.FERDIGSTILT,
                    relatertBehandlingId = null,
                    vilkårsvurderinger = emptyList()
            ),
            søker = SøkerDto(
                    aktivitetskrav = AktivitetskravDto(
                            aktivitetspliktInntrefferDato = LocalDate.of(2021, 5, 1),
                            harSagtOppArbeidsforhold = false
                    ),
                    personIdent = "12345678910",
                    barn = emptyList(),
                    tilhørendeEnhet = "4489",
                    kode6eller7 = false
            ),
            vedtak = VedtaksdetaljerDto(
                    vedtak = Vedtak.INNVILGET,
                    vedtaksdato = LocalDate.of(2021, 5, 12),
                    opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                    saksbehandlerId = "A12345",
                    beslutterId = "B23456",
                    tilkjentYtelse = tilkjentYtelse,
                    inntekter = listOf(inntekt)
            )
    )
}

fun opprettIverksett(behandlingId: UUID): Iverksett {

    val inntekt = Inntekt(
            periodebeløp = Periodebeløp(
                    beløp = 150000,
                    periodetype = Periodetype.MÅNED,
                    fraOgMed = LocalDate.of(2021, 1, 1),
                    tilOgMed = LocalDate.of(2021, 12, 31)
            ), inntektstype = null
    )

    val andelTilkjentYtelse = AndelTilkjentYtelse(
            periodebeløp = Periodebeløp(
                    beløp = 5000,
                    periodetype = Periodetype.MÅNED,
                    fraOgMed = LocalDate.of(2021, 1, 1),
                    tilOgMed = LocalDate.of(2021, 12, 31)
            ),
            kildeBehandlingId = UUID.randomUUID(),
            periodeId = 1
    )
    val tilkjentYtelse = TilkjentYtelse(
            id = UUID.randomUUID(),
            utbetalingsoppdrag = null,
            status = TilkjentYtelseStatus.AKTIV,
            andelerTilkjentYtelse = listOf(andelTilkjentYtelse)
    )

    return Iverksett(
            fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
            behandling = Behandlingsdetaljer(
                    behandlingId = behandlingId,
                    forrigeBehandlingId = null,
                    eksternId = 9L,
                    behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                    behandlingÅrsak = BehandlingÅrsak.SØKNAD,
                    behandlingResultat = BehandlingResultat.FERDIGSTILT,
                    relatertBehandlingId = null,
                    vilkårsvurderinger = emptyList()
            ),
            søker = Søker(
                    aktivitetskrav = Aktivitetskrav(
                            aktivitetspliktInntrefferDato = LocalDate.of(2021, 5, 1),
                            harSagtOppArbeidsforhold = false
                    ),
                    personIdent = "12345678910",
                    barn = emptyList(),
                    tilhørendeEnhet = "4489",
                    kode6eller7 = false
            ),
            vedtak = Vedtaksdetaljer(
                    vedtak = Vedtak.INNVILGET,
                    vedtaksdato = LocalDate.of(2021, 5, 12),
                    opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                    saksbehandlerId = "A12345",
                    beslutterId = "B23456",
                    tilkjentYtelse = tilkjentYtelse,
                    inntekter = listOf(inntekt)
            )
    )
}

fun opprettBrev(): Brev {
    return Brev(UUID.fromString("234bed7c-b1d3-11eb-8529-0242ac130003"), ByteArray(256))
}

fun opprettTilkjentYtelse(behandlingId: UUID): TilkjentYtelse {

    return TilkjentYtelse(
            id = behandlingId,
            utbetalingsoppdrag = null,
            andelerTilkjentYtelse = listOf(
                    AndelTilkjentYtelse(
                            periodebeløp = Periodebeløp(
                                    beløp = 100,
                                    Periodetype.MÅNED,
                                    fraOgMed = LocalDate.now(),
                                    tilOgMed = LocalDate.now().plusMonths(1)
                            ),
                            periodeId = 1L,
                            forrigePeriodeId = 1L,
                            kildeBehandlingId = UUID.randomUUID()
                    )
            )
    )
}

class IverksettResultatMockBuilder private constructor(

        val tilkjentYtelse: TilkjentYtelse,
        val oppdragResultat: OppdragResultat,
        val journalpostResultat: JournalpostResultat,
        val vedtaksbrevResultat: DistribuerVedtaksbrevResultat
) {

    data class Builder(
            var oppdragResultat: OppdragResultat? = null,
            var journalpostResultat: JournalpostResultat? = null,
            var vedtaksbrevResultat: DistribuerVedtaksbrevResultat? = null
    ) {

        fun oppdragResultat(oppdragResultat: OppdragResultat) = apply { this.oppdragResultat = oppdragResultat }
        fun journalPostResultat() = apply { this.journalpostResultat = JournalpostResultat(UUID.randomUUID().toString()) }
        fun vedtaksbrevResultat(behandlingId: UUID) =
                apply { this.vedtaksbrevResultat = DistribuerVedtaksbrevResultat(bestillingId = behandlingId.toString()) }

        fun build(behandlingId: UUID, tilkjentYtelse: TilkjentYtelse?) =
                IverksettResultat(behandlingId, tilkjentYtelse, oppdragResultat, journalpostResultat, vedtaksbrevResultat)
    }
}

