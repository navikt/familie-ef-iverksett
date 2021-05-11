package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.domene.*
import no.nav.familie.ef.iverksett.infrastruktur.json.*
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

fun opprettIverksettJson(behandlingId: String): IverksettJson {

    val tilkjentYtelseJson = TilkjentYtelseJson(
        id = UUID.randomUUID(),
        behandlingId = UUID.randomUUID(),
        personident = "personident",
        status = TilkjentYtelseStatus.AKTIV,
        type = TilkjentYtelseType.ENDRING,
        andelerTilkjentYtelse = emptyList()
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
        fagsakId = "1",
        saksnummer = "1",
        behandlingId = behandlingId,
        eksternId = 1L,
        relatertBehandlingId = "2",
        kode6eller7 = false,
        tidspunktVedtak = OffsetDateTime.now(),
        vilkårsvurderinger = emptyList(),
        personIdent = "12345678910",
        barn = emptyList(),
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingResultat = BehandlingResultat.FERDIGSTILT,
        opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
        aktivitetskrav = AktivitetskravJson(LocalDate.now(), false),
        funksjonellId = "0",
        behandlingÅrsak = BehandlingÅrsak.SØKNAD
    )
}

fun opprettBrev(): Brev {
    return Brev("234bed7c-b1d3-11eb-8529-0242ac130003", ByteArray(256))
}