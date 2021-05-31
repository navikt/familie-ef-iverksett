package no.nav.familie.ef.iverksett.iverksetting.domene

import java.util.UUID

data class AndelTilkjentYtelse(val periodebeløp: Periodebeløp,
                               val periodeId: Long? = null,
                               val forrigePeriodeId: Long? = null,
                               val kildeBehandlingId: UUID? = null) {

    private fun erTilsvarendeForUtbetaling(other: AndelTilkjentYtelse): Boolean {
        return (this.periodebeløp.fraOgMed == other.periodebeløp.fraOgMed
                && this.periodebeløp.tilOgMed == other.periodebeløp.tilOgMed
                && this.periodebeløp.beløp == other.periodebeløp.beløp
               )
    }

    fun erNull() = this.periodebeløp.beløp == 0

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