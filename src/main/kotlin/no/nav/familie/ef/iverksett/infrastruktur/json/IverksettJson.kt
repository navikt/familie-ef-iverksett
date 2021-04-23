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
    return Delvilkårsvurdering(this.resultat, this.vurderinger.map { it.toDomain() })
}

fun VilkårsvurderingJson.toDomain(): Vilkårsvurdering {
    return Vilkårsvurdering(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toDomain() })
}

fun AktivitetskravJson.toDomain(): Aktivitetskrav {
    return Aktivitetskrav(this.aktivitetspliktInntrefferDato, this.harSagtOppArbeidsforhold)
}

fun IverksettJson.toDomain(): Iverksett {
    return Iverksett(
        this.brev.map { it.toDomain() },
        this.forrigeTilkjentYtelse.map { it.toDomain() },
        this.tilkjentYtelse.map { it.toDomain() },
        this.fagsakId,
        this.saksnummer,
        this.behandlingId,
        this.relatertBehandlingId,
        this.kode6eller7,
        this.tidspunktVedtak,
        this.vilkårsvurderinger.map { it.toDomain() },
        this.person.toDomain(),
        this.barn.map { it.toDomain() },
        this.behandlingType,
        this.behandlingÅrsak,
        this.behandlingResultat,
        this.vedtak,
        this.opphørÅrsak,
        this.utbetalinger.map { it.toDomain() },
        this.inntekt.map { it.toDomain() },
        this.inntektsReduksjon.map { it.toDomain() },
        this.aktivitetskrav.toDomain(),
        this.funksjonellId
    )
}
