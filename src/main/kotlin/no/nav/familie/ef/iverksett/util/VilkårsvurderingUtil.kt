package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.eksterne.kontrakter.ef.AktivitetsvilkårBarnetilsyn
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.SvarId

object VilkårsvurderingUtil {

    fun hentHarSagtOppEllerRedusertFraVurderinger(vilkårsvurderinger: List<Vilkårsvurdering>): Boolean? {
        val vilkårsvurdering = vilkårsvurderinger.find { it.vilkårType == VilkårType.SAGT_OPP_ELLER_REDUSERT }
            ?: error("Finner ikke vurderingen for sagt opp eller redusert")

        return if (vilkårsvurdering.resultat == Vilkårsresultat.SKAL_IKKE_VURDERES) {
            null
        } else {
            val vurdering = vilkårsvurdering.delvilkårsvurderinger.flatMap { it.vurderinger }
                .firstOrNull { it.regelId == RegelId.SAGT_OPP_ELLER_REDUSERT }
                ?: error("Finner ikke delvilkårsvurderingen for sagt opp eller redusert stilling")
            harSagtOppEllerRedusertStilling(vurdering.svar)
        }
    }

    fun hentAktivitetsvilkårBarnetilsyn(vilkårsvurderinger: List<Vilkårsvurdering>): AktivitetsvilkårBarnetilsyn? {
        val vilkårsvurdering = vilkårsvurderinger.find { it.vilkårType == VilkårType.AKTIVITET_ARBEID }
            ?: error("Finner ikke vurderingen for arbeid aktivitet barnetilsyn")

        val delvikår = vilkårsvurdering.delvilkårsvurderinger.first().vurderinger
            .find { it.regelId == RegelId.ER_I_ARBEID_ELLER_FORBIGÅENDE_SYKDOM }
            ?: error("Finner ikke delvilkårvurderingen for arbeid aktivitet barnetilsyn")

        return delvikår.svar?.let { AktivitetsvilkårBarnetilsyn.valueOf(it.name) }
    }

    private fun harSagtOppEllerRedusertStilling(svarId: SvarId?) = when (svarId) {
        SvarId.JA -> true
        SvarId.NEI -> false
        else -> null
    }
}
