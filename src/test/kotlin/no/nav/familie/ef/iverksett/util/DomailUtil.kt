package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.domene.BehandlingResultat
import no.nav.familie.ef.iverksett.domene.BehandlingType
import no.nav.familie.ef.iverksett.domene.BehandlingÅrsak
import no.nav.familie.ef.iverksett.domene.InntektsType
import no.nav.familie.ef.iverksett.domene.OpphørÅrsak
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.Periodetype
import no.nav.familie.ef.iverksett.domene.Vedtak
import no.nav.familie.ef.iverksett.infrastruktur.json.AktivitetskravJson
import no.nav.familie.ef.iverksett.infrastruktur.json.AndeltilkjentYtelseJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevdataJson
import no.nav.familie.ef.iverksett.infrastruktur.json.InntektJson
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonJson
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate

fun opprettIverksettJson(behandlingId: String,
                         brev: List<BrevJson>,
                         tidspunktVedtak: LocalDate? = LocalDate.now(),
                         aktivitetspliktInntrefferDato: LocalDate? = LocalDate.now()): IverksettJson {
    return IverksettJson(
            brev = brev,
            vedtak = Vedtak.INNVILGET,
            forrigeTilkjentYtelse = emptyList(),
            tilkjentYtelse = listOf(AndeltilkjentYtelseJson(
                    Periodebeløp(1000, Periodetype.MÅNED, LocalDate.parse("2021-01-01"), LocalDate.parse("2021-02-01")),
                    personIdent = "12345678910", stønadsType = StønadType.OVERGANGSSTØNAD, periodeId = 1L)),
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
            person = PersonJson(personIdent = "12345678910", aktorId = null),
            barn = emptyList(),
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingResultat = BehandlingResultat.FERDIGSTILT,
            opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
            aktivitetskrav = AktivitetskravJson(aktivitetspliktInntrefferDato!!, false),
            funksjonellId = "0",
            behandlingÅrsak = BehandlingÅrsak.SØKNAD
    )
}

fun opprettBrev(journalpostId: String): BrevJson {
    return BrevJson(
            journalpostId, BrevdataJson("mottaker", "saksbehandler"))
}