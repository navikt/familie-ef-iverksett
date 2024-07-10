package no.nav.familie.ef.iverksett.util

import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.Vedtaksdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad

fun IverksettData.copy(vedtak: Vedtaksdetaljer): IverksettData =
    when (this) {
        is IverksettOvergangsstønad -> this.copy(vedtak = vedtak as VedtaksdetaljerOvergangsstønad)
        else -> error("Ingen støtte ennå")
    }

fun Vedtaksdetaljer.copy(brevmottakere: Brevmottakere): Vedtaksdetaljer =
    when (this) {
        is VedtaksdetaljerOvergangsstønad -> this.copy(brevmottakere = brevmottakere)
        else -> error("Ingen støtte ennå")
    }

fun Vedtaksdetaljer.copy(tilkjentYtelse: TilkjentYtelse): Vedtaksdetaljer =
    when (this) {
        is VedtaksdetaljerOvergangsstønad -> this.copy(tilkjentYtelse = tilkjentYtelse)
        else -> error("Ingen støtte ennå")
    }
