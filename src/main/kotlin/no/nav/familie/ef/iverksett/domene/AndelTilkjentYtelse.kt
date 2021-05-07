package no.nav.familie.ef.iverksett.domene

import no.nav.familie.ef.iverksett.infrastruktur.json.Inntektstype
import no.nav.familie.kontrakter.ef.felles.StønadType
import java.util.*

data class AndelTilkjentYtelse(val periodebeløp: Periodebeløp,
                               val personIdent: String,
                               val periodeId: Long? = null,
                               val forrigePeriodeId: Long? = null,
                               val stønadsType: StønadType? = null,
                               val kildeBehandlingId: UUID? = null,
                               val inntektbeløp: Periodebeløp? = null,
                               val inntektstype: Inntektstype? = null) {

    private fun erTilsvarendeForUtbetaling(other: AndelTilkjentYtelse): Boolean {
        return (this.personIdent == other.personIdent
                && this.periodebeløp.fraOgMed == other.periodebeløp.fraOgMed
                && this.periodebeløp.tilOgMed == other.periodebeløp.tilOgMed
                && this.periodebeløp.utbetaltPerPeriode == other.periodebeløp.utbetaltPerPeriode
               )
    }

    fun erNull() = this.periodebeløp.utbetaltPerPeriode == 0

    companion object {

        /**
         * Merk at det søkes snitt på visse attributter (erTilsvarendeForUtbetaling)
         * og man kun returnerer objekter fra receiver (ikke other)
         */
        fun Set<AndelTilkjentYtelse>.snittAndeler(other: Set<AndelTilkjentYtelse>): Set<AndelTilkjentYtelse> {
            val andelerKunIDenne = this.subtractAndeler(other)
            return this.subtractAndeler(andelerKunIDenne)
        }

        fun Set<AndelTilkjentYtelse>.disjunkteAndeler(other: Set<AndelTilkjentYtelse>): Set<AndelTilkjentYtelse> {
            val andelerKunIDenne = this.subtractAndeler(other)
            val andelerKunIAnnen = other.subtractAndeler(this)
            return andelerKunIDenne.union(andelerKunIAnnen)
        }

        private fun Set<AndelTilkjentYtelse>.subtractAndeler(other: Set<AndelTilkjentYtelse>): Set<AndelTilkjentYtelse> {
            return this.filter { a ->
                other.none { b -> a.erTilsvarendeForUtbetaling(b) }
            }.toSet()
        }
    }
}