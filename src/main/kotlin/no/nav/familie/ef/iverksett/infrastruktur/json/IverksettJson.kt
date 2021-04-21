package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class IverksettJson(
    val brev: List<BrevJson> = emptyList(),
    val forrigeTilkjentYtelse: List<UtbetalingJson> = emptyList(),
    val tilkjentYtelse: List<UtbetalingJson> = emptyList(),
    val fagsakId: String,
    val saksnummer: String? = null,
    val behandlingId: String,
    val relatertBehandlingId: String? = null,
    val kode6eller7: Boolean,
    val tidspunktVedtak: ZonedDateTime? = null,
    val vilkårsvurderinger: List<VilkårsvurderingJson> = emptyList(),
    val person: PersonJson,
    val barn: List<PersonJson> = ArrayList(),
    val behandlingType: BehandlingTypeJson,
    val behandlingÅrsak: BehandlingÅrsakJson,
    val behandlingResultat: BehandlingResultatJson,
    val vedtak: VedtakJson? = null,
    val opphørÅrsak: OpphørÅrsakJson,
    val utbetalinger: List<UtbetalingJson>,
    val inntekt: List<InntektJson> = ArrayList(),
    val inntektsReduksjon: List<InntektsreduksjonJson> = emptyList(),
    val aktivitetskrav: AktivitetskravJson,
    val funksjonellId: String
)

data class AktivitetskravJson(

    @JsonSerialize(using = ToStringSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val aktivitetspliktInntrefferDato: LocalDate,
    val harSagtOppArbeidsforhold: Boolean
)

data class VilkårsvurderingJson(
    val vilkårType: VilkårTypeJson,
    val resultat: VilkårsresultatJson,
    val delvilkårsvurderinger: List<DelvilkårsvurderingJson> = emptyList()
)

data class DelvilkårsvurderingJson(
    val resultat: VilkårsresultatJson,
    val vurderinger: List<VurderingJson> = emptyList()
)

data class VurderingJson(
    val regelId: RegelIdJson,
    val svar: SvarIdJson? = null,
    val begrunnelse: String? = null
)

enum class VilkårsresultatJson(val beskrivelse: String) {
    OPPFYLT("Vilkåret er oppfylt når alle delvilkår er oppfylte"),
    IKKE_OPPFYLT("Vilkåret er ikke oppfylt hvis alle delvilkår er oppfylt eller ikke oppfylt, men minimum 1 ikke oppfylt"),
    IKKE_AKTUELL("Hvis søknaden/pdl data inneholder noe som gjør att delvilkåret ikke må besvares"),
    IKKE_TATT_STILLING_TIL("Init state, eller att brukeren ikke svaret på hele delvilkåret"),
    SKAL_IKKE_VURDERES("Saksbehandleren kan sette att ett delvilkår ikke skal vurderes");

    fun oppfyltEllerIkkeOppfylt() = this == OPPFYLT || this == IKKE_OPPFYLT
}

enum class VilkårTypeJson(val beskrivelse: String) {

    FORUTGÅENDE_MEDLEMSKAP("§15-2 Forutgående medlemskap"),
    LOVLIG_OPPHOLD("§15-3 Lovlig opphold"),

    MOR_ELLER_FAR("§15-4 Mor eller far"),

    SIVILSTAND("§15-4 Sivilstand"),
    SAMLIV("§15-4 Samliv"),
    ALENEOMSORG("§15-4 Aleneomsorg"),
    NYTT_BARN_SAMME_PARTNER("§15-4 Nytt barn samme partner"),
    SAGT_OPP_ELLER_REDUSERT("Sagt opp eller redusert stilling"),
    AKTIVITET("Aktivitet"),
    TIDLIGERE_VEDTAKSPERIODER("Tidligere vedtaksperioder");

    companion object {

        fun hentVilkår(): List<VilkårTypeJson> = values().toList()
    }
}

enum class VedtakJson {
    INNVILGET,
    DELVIS_INNVILGET,
    OPPHØRT,
    AVSLÅTT
}

enum class BehandlingTypeJson {
    SAKSBEHANDLINGSBLANKETT,
    FØRSTEGANGSBEHANDLING,
    REVURDERING,
    KLAGE,
    MIGRERING_FRA_INFOTRYGD,
    TILBAKEFØRING_TIL_INFOTRYGD
}

enum class BehandlingÅrsakJson {
    SØKNAD,
    PERIODISK_KONTROLL,
    ENDRET_SATS,
    DØDSFALL,
    NYE_OPPLYSNINGER,
    MIGRERING,
    TEKNISK_FEIL
}

enum class BehandlingResultatJson {
    FERDIGSTILT,
    DUPLIKAT,
    HENLAGT,
    ANNULLERT
}

enum class OpphørÅrsakJson {
    PERIODE_UTLØPT
}