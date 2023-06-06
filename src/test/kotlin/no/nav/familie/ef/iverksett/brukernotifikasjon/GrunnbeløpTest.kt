package no.nav.familie.ef.iverksett.brukernotifikasjon

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.lagIverksett
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.mockFeatureToggleService
import no.nav.familie.ef.iverksett.util.opprettIverksettDto
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class GrunnbeløpTest {

    // Denne testen vil feile hvis vi legger inn nytt grunnbeløp.
    // Vi ønsker at grunnbeløp skal sendes med fra ef-sak til iverksett.
    // Da kan denne testen og grunnbeløp slettes fra iverksett.
    @Test
    internal fun `Grunnbeløp 2023 halv G skal være rundet opp til 4943`() {
        Assertions.assertThat(halvG).isEqualTo(4943.toBigDecimal())
    }
}
