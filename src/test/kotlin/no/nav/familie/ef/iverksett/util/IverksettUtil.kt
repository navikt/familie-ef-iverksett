package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.iverksetting.domene.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad

fun Iverksett.copy(vedtak: Vedtaksdetaljer): Iverksett {
    return when(this){
        is IverksettOvergangsstønad -> this.copy(vedtak = vedtak as VedtaksdetaljerOvergangsstønad)
        else -> error("Ingen støtte ennå")
    }
}

fun Vedtaksdetaljer.copy(brevmottakere: Brevmottakere): Vedtaksdetaljer {
    return when(this) {
        is VedtaksdetaljerOvergangsstønad -> this.copy(brevmottakere = brevmottakere)
        else -> error("Ingen støtte ennå")
    }
}

fun Vedtaksdetaljer.copy(tilkjentYtelse: TilkjentYtelse): Vedtaksdetaljer {
    return when(this) {
        is VedtaksdetaljerOvergangsstønad -> this.copy(tilkjentYtelse = tilkjentYtelse)
        else -> error("Ingen støtte ennå")
    }
}