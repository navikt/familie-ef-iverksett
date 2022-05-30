package no.nav.familie.ef.iverksett.cucumber.steps

import io.cucumber.datatable.DataTable
import io.cucumber.java.no.Gitt
import io.cucumber.java.no.Når
import io.cucumber.java.no.Så
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator

class OppdragStepDefinitions {

    lateinit var nyTilkjentYtelse: TilkjentYtelseMedMetaData
    lateinit var forrigeTilkjentYtelse: TilkjentYtelse

    @Gitt("følgende oppdrag")
    fun følgende_oppdrag(dataTable: DataTable) {
    }

    @Når("lagTilkjentYtelseMedUtbetalingsoppdrag")
    fun lagTilkjentYtelseMedUtbetalingsoppdrag() {
        UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(nyTilkjentYtelse, forrigeTilkjentYtelse)
    }

    @Så("forvent følgende tilkjent ytelse")
    fun forventTilkjentYtelse() {
    }
}
