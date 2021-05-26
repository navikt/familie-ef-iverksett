package no.nav.familie.ef.iverksett.domene

import no.nav.familie.kontrakter.ef.felles.StønadType
import java.time.LocalDate
import java.util.ArrayList
import java.util.UUID

data class Iverksett(
        val fagsak: Fagsakdetaljer,
        val behandling: Behandlingsdetaljer,
        val søker: Søker,
        val vedtak: Vedtaksdetaljer,
)

data class Fagsakdetaljer(
        val fagsakId: UUID,
        val eksternId: Long,
        val stønadstype: StønadType
)

data class Søker(
        val aktivitetskrav: Aktivitetskrav,
        val personIdent: String,
        val barn: List<Barn> = ArrayList(),
        val tilhørendeEnhet: String,
        val kode6eller7: Boolean,
)

data class Vedtaksdetaljer(
        val vedtak: Vedtak,
        val vedtaksdato: LocalDate,
        val opphørÅrsak: OpphørÅrsak?,
        val saksbehandlerId: String,
        val beslutterId: String,
        val tilkjentYtelse: TilkjentYtelse,
        val inntekter: List<Inntekt>
)

data class Behandlingsdetaljer(
        val forrigeBehandlingId: UUID? = null,
        val behandlingId: UUID,
        val eksternId: Long,
        val behandlingType: BehandlingType,
        val behandlingÅrsak: BehandlingÅrsak,
        val behandlingResultat: BehandlingResultat,
        val relatertBehandlingId: UUID? = null,
        val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList(),

        )

data class Aktivitetskrav(
        val aktivitetspliktInntrefferDato: LocalDate,
        val harSagtOppArbeidsforhold: Boolean
)

data class Vilkårsvurdering(
        val vilkårType: VilkårType,
        val resultat: Vilkårsresultat,
        val delvilkårsvurderinger: List<Delvilkårsvurdering> = emptyList()
)

data class Delvilkårsvurdering(
        val resultat: Vilkårsresultat,
        val vurderinger: List<Vurdering> = emptyList()
)

data class Vurdering(
        val regelId: RegelId,
        val svar: SvarId? = null,
        val begrunnelse: String? = null
)

enum class Vilkårsresultat(val beskrivelse: String) {
    OPPFYLT("Vilkåret er oppfylt når alle delvilkår er oppfylte"),
    IKKE_OPPFYLT("Vilkåret er ikke oppfylt hvis alle delvilkår er oppfylt eller ikke oppfylt, men minimum 1 ikke oppfylt"),
    IKKE_AKTUELL("Hvis søknaden/pdl data inneholder noe som gjør att delvilkåret ikke må besvares"),
    IKKE_TATT_STILLING_TIL("Init state, eller att brukeren ikke svaret på hele delvilkåret"),
    SKAL_IKKE_VURDERES("Saksbehandleren kan sette att ett delvilkår ikke skal vurderes");

    fun oppfyltEllerIkkeOppfylt() = this == OPPFYLT || this == IKKE_OPPFYLT
}

enum class VilkårType(val beskrivelse: String) {

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

        fun hentVilkår(): List<VilkårType> = values().toList()
    }
}

enum class Vedtak {
    INNVILGET,
    DELVIS_INNVILGET,
    OPPHØRT,
    AVSLÅTT
}

enum class BehandlingType {
    SAKSBEHANDLINGSBLANKETT,
    FØRSTEGANGSBEHANDLING,
    REVURDERING,
    KLAGE,
    MIGRERING_FRA_INFOTRYGD,
    TILBAKEFØRING_TIL_INFOTRYGD
}

enum class BehandlingÅrsak {
    SØKNAD,
    PERIODISK_KONTROLL,
    ENDRET_SATS,
    DØDSFALL,
    NYE_OPPLYSNINGER,
    MIGRERING,
    TEKNISK_FEIL
}

enum class BehandlingResultat {
    FERDIGSTILT,
    DUPLIKAT,
    HENLAGT,
    ANNULLERT
}

enum class OpphørÅrsak {
    PERIODE_UTLØPT
}

enum class IverksettStatus {
    SENDT_TIL_OPPDRAG,
    FEILET_MOT_OPPDRAG,
    OK_MOT_OPPDRAG,
    JOURNALFØRT,
    OK,
    IKKE_PÅBEGYNT
}
