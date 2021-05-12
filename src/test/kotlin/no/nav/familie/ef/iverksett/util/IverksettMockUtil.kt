package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.domene.*
import no.nav.familie.ef.iverksett.infrastruktur.json.*
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

fun opprettIverksettJson(behandlingId: String, tidspunktVedtak: LocalDate? = LocalDate.now()): IverksettJson {

    val tilkjentYtelseJson = TilkjentYtelseJson(
            id = UUID.randomUUID(),
            behandlingId = UUID.randomUUID(),
            personident = "personident",
            status = TilkjentYtelseStatus.AKTIV,
            type = TilkjentYtelseType.ENDRING,
            andelerTilkjentYtelse = listOf(AndelTilkjentYtelseJson(Periodebeløp(1000,
                                                                                Periodetype.MÅNED,
                                                                                LocalDate.parse("2021-01-01"),
                                                                                LocalDate.parse("2021-02-01")),
                                                                   personIdent = "12345678910",
                                                                   stønadsType = StønadType.OVERGANGSSTØNAD,
                                                                   periodeId = 1L))
    )

    return IverksettJson(
            forrigeTilkjentYtelse = tilkjentYtelseJson,
            tilkjentYtelse = TilkjentYtelseMedMetadataJson(
                    tilkjentYtelseJson = tilkjentYtelseJson,
                    saksbehandlerId = "saksbehandlerid",
                    eksternBehandlingId = 0,
                    stønadstype = StønadType.OVERGANGSSTØNAD,
                    eksternFagsakId = 0
            ),
            inntekt = listOf(InntektJson(Periodebeløp(1000,
                                                      Periodetype.MÅNED,
                                                      LocalDate.parse("2021-01-01"),
                                                      LocalDate.parse("2021-02-01")),
                                         InntektsType.ARBEIDINNTEKT)),
            fagsakId = "1",
            saksnummer = "1",
            behandlingId = behandlingId,
            eksternId = 1L,
            relatertBehandlingId = "2",
            kode6eller7 = false,
            tidspunktVedtak = tidspunktVedtak,
            vilkårsvurderinger = emptyList(),
            personIdent = "12345678910",
            barn = emptyList(),
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingResultat = BehandlingResultat.FERDIGSTILT,
            opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
            aktivitetskrav = AktivitetskravJson(LocalDate.parse("2021-02-01"), false),
            funksjonellId = "0",
            behandlingÅrsak = BehandlingÅrsak.SØKNAD
    )
}

fun opprettBrev(): Brev {
    return Brev("234bed7c-b1d3-11eb-8529-0242ac130003", ByteArray(256))
}