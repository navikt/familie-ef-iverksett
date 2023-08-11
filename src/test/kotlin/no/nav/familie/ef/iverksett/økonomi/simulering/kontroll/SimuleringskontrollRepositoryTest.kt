package no.nav.familie.ef.iverksett.økonomi.simulering.kontroll

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.beriketSimuleringsresultat
import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.ef.iverksett.util.opprettTilkjentYtelseMedMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class SimuleringskontrollRepositoryTest : ServerTest() {

    @Autowired
    lateinit var repository: SimuleringskontrollRepository

    @Test
    fun `lagring og henting av data`() {
        val behandlingId = UUID.randomUUID()
        val input = SimuleringskontrollInput(Simulering(opprettTilkjentYtelseMedMetadata(), UUID.randomUUID()), beriketSimuleringsresultat())
        val resultat = SimuleringskontrollResultat(beriketSimuleringsresultat())
        val simuleringskontroll = repository.insert(Simuleringskontroll(behandlingId, input, resultat))

        val hentetSimuleringskontroll = repository.findByIdOrThrow(behandlingId)

        assertThat(hentetSimuleringskontroll).isEqualTo(simuleringskontroll)
    }
}
