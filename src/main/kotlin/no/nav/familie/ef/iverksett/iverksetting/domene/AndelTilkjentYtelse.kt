package no.nav.familie.ef.iverksett.iverksetting.domene

import no.nav.familie.kontrakter.ef.iverksett.Periodetype
import java.time.LocalDate
import java.util.UUID

data class AndelTilkjentYtelse(val beløp: Int,
                               val fraOgMed: LocalDate,
                               val tilOgMed: LocalDate,
                               val periodetype: Periodetype,

                               val inntekt: Int,
                               val samordningsfradrag: Int,
                               val inntektsreduksjon: Int,

                               val periodeId: Long? = null,
                               val forrigePeriodeId: Long? = null,
                               val kildeBehandlingId: UUID? = null) {

    private fun erTilsvarendeForUtbetaling(other: AndelTilkjentYtelse): Boolean {
        return (this.fraOgMed == other.fraOgMed
                && this.tilOgMed == other.tilOgMed
                && this.beløp == other.beløp)
    }

    fun erNull() = this.beløp == 0

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