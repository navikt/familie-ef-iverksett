package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.familie.ef.iverksett.iverksetting.domene.Vurdering
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.SvarId

object VedtakstatistikkMapperTestHelper {
    val forutgåendeMedlemskap =
        Vilkårsvurdering(
            vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                                    SvarId.JA,
                                    null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.MEDLEMSKAP_UNNTAK,
                                    SvarId.NEI,
                                    null,
                                ),
                            ),
                    ),
                ),
        )

    val forutgåendeMedlemskapEøs =
        Vilkårsvurdering(
            vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                                    SvarId.NEI,
                                    null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.MEDLEMSKAP_UNNTAK,
                                    SvarId.MEDLEM_MER_ENN_5_ÅR_EØS,
                                    null,
                                ),
                            ),
                    ),
                ),
        )

    val forutgåendeMedlemskapEøsAnnenForelder =
        Vilkårsvurdering(
            vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                                    SvarId.NEI,
                                    null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.MEDLEMSKAP_UNNTAK,
                                    SvarId.MEDLEM_MER_ENN_5_ÅR_EØS_ANNEN_FORELDER_TRYGDEDEKKET_I_NORGE,
                                    null,
                                ),
                            ),
                    ),
                ),
        )
    val lovligOpphold =
        Vilkårsvurdering(
            vilkårType = VilkårType.LOVLIG_OPPHOLD,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.BOR_OG_OPPHOLDER_SEG_I_NORGE,
                                    SvarId.JA,
                                    null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.OPPHOLD_UNNTAK,
                                    SvarId.NEI,
                                    null,
                                ),
                            ),
                    ),
                ),
        )

    val lovligOppholdEøs =
        Vilkårsvurdering(
            vilkårType = VilkårType.LOVLIG_OPPHOLD,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.BOR_OG_OPPHOLDER_SEG_I_NORGE,
                                    SvarId.NEI,
                                    null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.OPPHOLD_UNNTAK,
                                    SvarId.OPPHOLDER_SEG_I_ANNET_EØS_LAND,
                                    null,
                                ),
                            ),
                    ),
                ),
        )
    val morEllerFar =
        Vilkårsvurdering(
            vilkårType = VilkårType.MOR_ELLER_FAR,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.OMSORG_FOR_EGNE_ELLER_ADOPTERTE_BARN,
                                SvarId.JA,
                                null,
                            ),
                        ),
                    ),
                ),
        )
    val sivilstand =
        Vilkårsvurdering(
            vilkårType = VilkårType.SIVILSTAND,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.KRAV_SIVILSTAND_UTEN_PÅKREVD_BEGRUNNELSE,
                                SvarId.JA,
                                null,
                            ),
                        ),
                    ),
                ),
        )
    val samliv =
        Vilkårsvurdering(
            vilkårType = VilkårType.SAMLIV,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.LEVER_IKKE_I_EKTESKAPLIGNENDE_FORHOLD,
                                SvarId.JA,
                                null,
                            ),
                        ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.LEVER_IKKE_MED_ANNEN_FORELDER,
                                SvarId.JA,
                                null,
                            ),
                        ),
                    ),
                ),
        )
    val aleneomsorg =
        Vilkårsvurdering(
            vilkårType = VilkårType.ALENEOMSORG,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.SKRIFTLIG_AVTALE_OM_DELT_BOSTED,
                                    SvarId.NEI,
                                    null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    regelId = RegelId.NÆRE_BOFORHOLD,
                                    svar = SvarId.NEI,
                                    begrunnelse = null,
                                ),
                            ),
                    ),
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    regelId = RegelId.MER_AV_DAGLIG_OMSORG,
                                    svar = SvarId.JA,
                                    begrunnelse = null,
                                ),
                            ),
                    ),
                ),
        )
    val aleneomsorg2 =
        Vilkårsvurdering(
            vilkårType = VilkårType.ALENEOMSORG,
            resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
                        vurderinger = listOf(),
                    ),
                ),
        )
    val nyttBarnSammePartner =
        Vilkårsvurdering(
            vilkårType = VilkårType.NYTT_BARN_SAMME_PARTNER,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.HAR_FÅTT_ELLER_VENTER_NYTT_BARN_MED_SAMME_PARTNER,
                                SvarId.NEI,
                                null,
                            ),
                        ),
                    ),
                ),
        )
    val sagtOppRedusertStilling =
        Vilkårsvurdering(
            vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                                    svar = SvarId.NEI,
                                    begrunnelse = null,
                                ),
                            ),
                    ),
                ),
        )
    val aktivitet =
        Vilkårsvurdering(
            vilkårType = VilkårType.AKTIVITET,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger =
                            listOf(
                                Vurdering(
                                    RegelId.FYLLER_BRUKER_AKTIVITETSPLIKT,
                                    SvarId.JA,
                                    null,
                                ),
                            ),
                    ),
                ),
        )
    val tidligereVedtaksperioder =
        Vilkårsvurdering(
            vilkårType = VilkårType.TIDLIGERE_VEDTAKSPERIODER,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.HAR_TIDLIGERE_ANDRE_STØNADER_SOM_HAR_BETYDNING,
                                SvarId.NEI,
                                null,
                            ),
                        ),
                    ),
                    Delvilkårsvurdering(
                        Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.HAR_TIDLIGERE_MOTTATT_OVERGANSSTØNAD,
                                SvarId.NEI,
                                null,
                            ),
                        ),
                    ),
                ),
        )
    val arbeid =
        Vilkårsvurdering(
            vilkårType = VilkårType.AKTIVITET_ARBEID,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger =
                listOf(
                    Delvilkårsvurdering(
                        Vilkårsresultat.OPPFYLT,
                        listOf(
                            Vurdering(
                                RegelId.ER_I_ARBEID_ELLER_FORBIGÅENDE_SYKDOM,
                                SvarId.ER_I_ARBEID,
                                null,
                            ),
                        ),
                    ),
                ),
        )

    fun lagVilkårsvurderinger(): List<Vilkårsvurdering> =
        listOf(
            forutgåendeMedlemskap,
            lovligOpphold,
            morEllerFar,
            sivilstand,
            samliv,
            aleneomsorg,
            aleneomsorg2,
            nyttBarnSammePartner,
            sagtOppRedusertStilling,
            aktivitet,
            tidligereVedtaksperioder,
            arbeid,
        )

    fun lagVilkårsvurderingerEøsAnnenForelder(): List<Vilkårsvurdering> =
        listOf(
            forutgåendeMedlemskapEøsAnnenForelder,
            lovligOpphold,
            morEllerFar,
            sivilstand,
            samliv,
            aleneomsorg,
            aleneomsorg2,
            nyttBarnSammePartner,
            sagtOppRedusertStilling,
            aktivitet,
            tidligereVedtaksperioder,
            arbeid,
        )

    fun lagVilkårsvurderingerEøs(): List<Vilkårsvurdering> =
        listOf(
            forutgåendeMedlemskapEøs,
            lovligOpphold,
            morEllerFar,
            sivilstand,
            samliv,
            aleneomsorg,
            aleneomsorg2,
            nyttBarnSammePartner,
            sagtOppRedusertStilling,
            aktivitet,
            tidligereVedtaksperioder,
            arbeid,
        )

    fun lagVilkårsvurderingerEøsOgOpphold(): List<Vilkårsvurdering> =
        listOf(
            forutgåendeMedlemskapEøs,
            lovligOppholdEøs,
            morEllerFar,
            sivilstand,
            samliv,
            aleneomsorg,
            aleneomsorg2,
            nyttBarnSammePartner,
            sagtOppRedusertStilling,
            aktivitet,
            tidligereVedtaksperioder,
            arbeid,
        )
}
