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

    val forrigeTilkjentYtelse: TilkjentYtelseJson? = null,
    val tilkjentYtelse: TilkjentYtelseMedMetadataJson,
    val fagsakId: String,
    val saksnummer: String? = null,
    val behandlingId: String,
    val eksternId: Long,
    val relatertBehandlingId: String? = null,
    val kode6eller7: Boolean,
    val tidspunktVedtak: OffsetDateTime? = null,
    val vilkårsvurderinger: List<VilkårsvurderingJson> = emptyList(),
    val personIdent: String,
    val barn: List<PersonJson> = ArrayList(),
    val behandlingType: BehandlingType,
    val behandlingÅrsak: BehandlingÅrsak,
    val behandlingResultat: BehandlingResultat,
    val vedtak: Vedtak? = null,
    val opphørÅrsak: OpphørÅrsak,
    val aktivitetskrav: AktivitetskravJson,
    val funksjonellId: String,
    val tilhørendeEnhet: String
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
        forrigeTilkjentYtelse = this.forrigeTilkjentYtelse?.toDomain(),
        tilkjentYtelse = this.tilkjentYtelse.toDomain(),
        fagsakId = this.fagsakId,
        saksnummer = this.saksnummer,
        behandlingId = this.behandlingId,
        eksternId = this.eksternId,
        relatertBehandlingId = this.relatertBehandlingId,
        kode6eller7 = this.kode6eller7,
        tidspunktVedtak = this.tidspunktVedtak,
        vilkårsvurderinger = this.vilkårsvurderinger.map { it.toDomain() },
        personIdent = this.personIdent,
        barn = this.barn.map { it.toDomain() },
        behandlingType = this.behandlingType,
        behandlingÅrsak = this.behandlingÅrsak,
        behandlingResultat = this.behandlingResultat,
        vedtak = this.vedtak,
        opphørÅrsak = this.opphørÅrsak,
        aktivitetskrav = this.aktivitetskrav.toDomain(),
        funksjonellId = this.funksjonellId,
        tilhørendeEnhet = this.tilhørendeEnhet
    )
}
