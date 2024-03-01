package no.nav.familie.ef.iverksett.brukernotifikasjon

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class GrunnbeløpTest {
    // Denne testen vil feile hvis vi legger inn nytt grunnbeløp.
    // Vi ønsker at grunnbeløp skal sendes med fra ef-sak til iverksett.
    // Da kan denne testen og grunnbeløp slettes fra iverksett.
    @Test
    internal fun `Grunnbeløp 2023 halv G skal være rundet opp til 4943`() {
        Assertions.assertThat(halvG).isEqualTo(4943.toBigDecimal())
    }
}
