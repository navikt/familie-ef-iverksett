package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import org.junit.jupiter.api.Test
import no.nav.familie.eksterne.kontrakter.ef.Adressebeskyttelse as AdresseBeskyttelseEkstern
import no.nav.familie.eksterne.kontrakter.ef.AktivitetType as AktivitetTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType as BehandlingTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak as BehandlingÅrsakEkstern
import no.nav.familie.eksterne.kontrakter.ef.StønadType as StønadTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.VedtaksperiodeType as VedtakPeriodeTypeEkstern
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsresultat as VilkårsresultatEkstern

class FellesTilEksterneKontrakterEnumTest {

    @Test
    fun `for alle eksterne kontrakter enums, forvent fullstendig mapping fra familie kontrakter enums`() {
        Vedtaksresultat.values().forEach { Vedtak.valueOf(it.name) }
        BehandlingÅrsak.values().forEach { BehandlingÅrsakEkstern.valueOf(it.name) }
        BehandlingType.values().forEach { BehandlingTypeEkstern.valueOf(it.name) }
        Vilkårsresultat.values().forEach { VilkårsresultatEkstern.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { VedtakPeriodeTypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetTypeEkstern.valueOf(it.name) }
        AdressebeskyttelseGradering.values().forEach { AdresseBeskyttelseEkstern.valueOf(it.name) }
        StønadType.values().forEach { StønadTypeEkstern.valueOf(it.name) }
    }
}







