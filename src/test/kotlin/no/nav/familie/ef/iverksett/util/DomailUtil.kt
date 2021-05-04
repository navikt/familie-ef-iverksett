package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.domene.BehandlingResultat
import no.nav.familie.ef.iverksett.domene.BehandlingType
import no.nav.familie.ef.iverksett.domene.BehandlingÅrsak
import no.nav.familie.ef.iverksett.domene.OpphørÅrsak
import no.nav.familie.ef.iverksett.domene.Vedtak
import no.nav.familie.ef.iverksett.infrastruktur.json.AktivitetskravJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevdataJson
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonJson
import java.time.LocalDate
import java.time.OffsetDateTime

fun opprettIverksettJson(behandlingId: String, brev: List<BrevJson>): IverksettJson {
    return IverksettJson(
            brev = brev,
            vedtak = Vedtak.INNVILGET,
            forrigeTilkjentYtelse = emptyList(),
            tilkjentYtelse = emptyList(),
            fagsakId = "1",
            saksnummer = "1",
            behandlingId = behandlingId,
            eksternId = 1L,
            relatertBehandlingId = "2",
            kode6eller7 = false,
            tidspunktVedtak = OffsetDateTime.now(),
            vilkårsvurderinger = emptyList(),
            person = PersonJson(personIdent = "12345678910", aktorId = null),
            barn = emptyList(),
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingResultat = BehandlingResultat.FERDIGSTILT,
            opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
            aktivitetskrav = AktivitetskravJson(LocalDate.now(), false),
            funksjonellId = "0",
            behandlingÅrsak = BehandlingÅrsak.SØKNAD
    )
}

fun opprettBrev(journalpostId: String): BrevJson {
    return BrevJson(
            journalpostId, BrevdataJson("mottaker", "saksbehandler"))
}