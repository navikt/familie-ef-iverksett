package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.domene.*
import no.nav.familie.ef.iverksett.infrastruktur.json.*
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

fun opprettIverksettJson(behandlingId: String): IverksettDto {

    val tilkjentYtelse = TilkjentYtelseDto(
            id = UUID.randomUUID(),
            status = TilkjentYtelseStatus.AKTIV,
            type = TilkjentYtelseType.ENDRING,
            andelerTilkjentYtelse = emptyList()
    )

    return IverksettDto(
            fagsak = FagsakdetaljerDto(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
            behandling = BehandlingsdetaljerDto(behandlingId = UUID.randomUUID(),
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
                                        tidspunktVedtak = LocalDate.of(2021, 5, 12),
                                        opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                                        saksbehandlerId = "A12345",
                                        beslutterId = "B23456",
                                        tilkjentYtelse = tilkjentYtelse,
                                        inntekter = emptyList())
    )
}

fun opprettBrev(): Brev {
    return Brev("234bed7c-b1d3-11eb-8529-0242ac130003", ByteArray(256))
}