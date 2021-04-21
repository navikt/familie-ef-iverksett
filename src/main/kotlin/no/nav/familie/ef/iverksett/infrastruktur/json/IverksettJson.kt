package no.nav.familie.ef.iverksett.infrastruktur.json

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import no.nav.familie.ef.iverksett.domene.*
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

