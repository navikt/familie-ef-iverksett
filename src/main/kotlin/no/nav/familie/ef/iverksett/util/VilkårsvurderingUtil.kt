package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
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
            vilkårsvurdering.delvilkårsvurderinger.flatMap { it.vurderinger }
                    .firstOrNull { it.regelId == RegelId.SAGT_OPP_ELLER_REDUSERT }
                    ?.let { harSagtOppEllerRedusertStilling(it.svar) }
            ?: error("Finner ikke delvilkårsvurderingen for sagt opp eller redusert stilling")
        }
    }

    private fun harSagtOppEllerRedusertStilling(svarId: SvarId?) = when (svarId) {
        SvarId.JA -> true
        SvarId.NEI -> false
        else -> error("Sagt opp eller redusert har bara ja eller nej som svarslaternativ $svarId")

    }
}