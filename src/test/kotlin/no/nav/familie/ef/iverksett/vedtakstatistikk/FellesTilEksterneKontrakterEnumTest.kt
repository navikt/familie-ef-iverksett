package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.junit.jupiter.api.Test
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse as AdresseBeskyttelseEkstern
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType as AktivitetTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType as BehandlingTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak as BehandlingÅrsakEkstern
import no.nav.familie.eksterne.kontrakter.ef.StønadType as StønadTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType as VedtakPeriodeTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat as VilkårsresultatEkstern
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Vedtaksresultat as VedtaksresultatEkstern
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Periodetype as PeriodetypeEkstern
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Aktivitetstype as AktivitetstypeEkstern
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Stønadstype as StønadstypeEkstern

class FellesTilEksterneKontrakterEnumTest {

    @Test
    fun `for alle eksterne kontrakter enums, forvent fullstendig mapping fra familie kontrakter enums`() {
        Vedtaksresultat.values().forEach { Vedtak.valueOf(it.name) }
        BehandlingÅrsak.values().forEach { BehandlingÅrsakEkstern.valueOf(it.name) }
        BehandlingType.values().forEach { BehandlingTypeEkstern.valueOf(it.name) }
        Vilkårsresultat.values().forEach { VilkårsresultatEkstern.valueOf(it.name) }
        VilkårType.values().forEach { Vilkår.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { VedtakPeriodeTypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetTypeEkstern.valueOf(it.name) }
        AdressebeskyttelseGradering.values().forEach { AdresseBeskyttelseEkstern.valueOf(it.name) }
        StønadType.values().forEach { StønadTypeEkstern.valueOf(it.name) }
    }

    @Test
    fun `for alle arbeidsoppfølging enums i eksterne kontrakter, forvent mapping fra domene`() {
        Vedtaksresultat.values().forEach { VedtaksresultatEkstern.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { PeriodetypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetstypeEkstern.valueOf(it.name) }
        StønadType.values().forEach { StønadstypeEkstern.valueOf(it.name) }
    }
}
