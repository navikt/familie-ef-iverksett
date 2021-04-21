package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import no.nav.familie.ef.iverksett.domene.*
import java.time.LocalDate
import java.time.OffsetDateTime
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
    val tidspunktVedtak: OffsetDateTime? = null,
    val vilkårsvurderinger: List<VilkårsvurderingJson> = emptyList(),
    val person: PersonJson,
    val barn: List<PersonJson> = ArrayList(),
    val behandlingType: BehandlingType,
    val behandlingÅrsak: BehandlingÅrsak,
    val behandlingResultat: BehandlingResultat,
    val vedtak: Vedtak? = null,
    val opphørÅrsak: OpphørÅrsak,
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
    val vilkårType: VilkårType,
    val resultat: Vilkårsresultat,
    val delvilkårsvurderinger: List<DelvilkårsvurderingJson> = emptyList()
)

data class DelvilkårsvurderingJson(
    val resultat: Vilkårsresultat,
    val vurderinger: List<VurderingJson> = emptyList()
)

data class VurderingJson(
    val regelId: RegelId,
    val svar: SvarId? = null,
    val begrunnelse: String? = null
)

fun VurderingJson.toDomain(): Vurdering {
    return Vurdering(this.regelId, this.svar, this.begrunnelse)
}

fun DelvilkårsvurderingJson.toDomain(): Delvilkårsvurdering {
    return Delvilkårsvurdering(this.resultat, this.vurderinger.map { it.toDomain() }.toList())
}

fun VilkårsvurderingJson.toDomain(): Vilkårsvurdering {
    return Vilkårsvurdering(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toDomain() }.toList())
}

fun AktivitetskravJson.toDomain(): Aktivitetskrav {
    return Aktivitetskrav(this.aktivitetspliktInntrefferDato, this.harSagtOppArbeidsforhold)
}

fun IverksettJson.toDomain(): Iverksett {
    return Iverksett(
        this.brev.map { it.toDomain() }.toList(),
        this.forrigeTilkjentYtelse.map { it.toDomain() }.toList(),
        this.tilkjentYtelse.map { it.toDomain() }.toList(),
        this.fagsakId,
        this.saksnummer,
        this.behandlingId,
        this.relatertBehandlingId,
        this.kode6eller7,
        this.tidspunktVedtak,
        this.vilkårsvurderinger.map { it.toDomain() }.toList(),
        this.person.toDomain(),
        this.barn.map { it.toDomain() }.toList(),
        this.behandlingType,
        this.behandlingÅrsak,
        this.behandlingResultat,
        this.vedtak,
        this.opphørÅrsak,
        this.utbetalinger.map { it.toDomain() }.toList(),
        this.inntekt.map { it.toDomain() }.toList(),
        this.inntektsReduksjon.map { it.toDomain() }.toList(),
        this.aktivitetskrav.toDomain(),
        this.funksjonellId
    )
}

fun Vurdering.toJson(): VurderingJson {
    return VurderingJson(this.regelId, this.svar, this.begrunnelse)
}

fun Delvilkårsvurdering.toJson(): DelvilkårsvurderingJson {
    return DelvilkårsvurderingJson(this.resultat, this.vurderinger.map { it.toJson() }.toList())
}

fun Vilkårsvurdering.toJson(): VilkårsvurderingJson {
    return VilkårsvurderingJson(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toJson() }.toList())
}

fun Aktivitetskrav.toJson(): AktivitetskravJson {
    return AktivitetskravJson(this.aktivitetspliktInntrefferDato, this.harSagtOppArbeidsforhold)
}

fun Iverksett.toJson(): IverksettJson {
    return IverksettJson(
        this.brev.map { it.toJson() }.toList(),
        this.forrigeTilkjentYtelse.map { it.toJson() }.toList(),
        this.tilkjentYtelse.map { it.toJson() }.toList(),
        this.fagsakId,
        this.saksnummer,
        this.behandlingId,
        this.relatertBehandlingId,
        this.kode6eller7,
        this.tidspunktVedtak,
        this.vilkårsvurderinger.map { it.toJson() }.toList(),
        this.person.toJson(),
        this.barn.map { it.toJson() }.toList(),
        this.behandlingType,
        this.behandlingÅrsak,
        this.behandlingResultat,
        this.vedtak,
        this.opphørÅrsak,
        this.utbetalinger.map { it.toJson() }.toList(),
        this.inntekt.map { it.toJson() }.toList(),
        this.inntektsReduksjon.map { it.toJson() }.toList(),
        this.aktivitetskrav.toJson(),
        this.funksjonellId
    )
}
