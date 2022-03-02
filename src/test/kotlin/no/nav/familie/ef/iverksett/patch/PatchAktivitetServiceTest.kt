package no.nav.familie.ef.iverksett.patch

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.util.opprettIverksett
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class PatchAktivitetServiceTest : ServerTest() {


    @Autowired private lateinit var iverksettingRepository: IverksettingRepository
    @Autowired private lateinit var patchAktivitetService: PatchAktivitetService

    @Test
    internal fun `skal oppdatere iverksett`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = lagIverksettMedAktivitetMigrering(behandlingId, AktivitetType.MIGRERING)
        iverksettingRepository.lagre(behandlingId, iverksett, null)
        patchAktivitetService.patch(behandlingId, true)
        assertThat(iverksettingRepository.hent(behandlingId).vedtak.vedtaksperioder.single().aktivitet)
                .isEqualTo(AktivitetType.FORSØRGER_REELL_ARBEIDSSØKER)
    }

    @Test
    internal fun `skal ikke oppdatere iverksett hvis den ikke har riktig aktivitet`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = lagIverksettMedAktivitetMigrering(behandlingId, AktivitetType.IKKE_AKTIVITETSPLIKT)
        iverksettingRepository.lagre(behandlingId, iverksett, null)
        patchAktivitetService.patch(behandlingId, true)
        assertThat(iverksettingRepository.hent(behandlingId).vedtak.vedtaksperioder.single().aktivitet)
                .isEqualTo(AktivitetType.IKKE_AKTIVITETSPLIKT)
    }

    private fun lagIverksettMedAktivitetMigrering(behandlingId: UUID, aktivitetType: AktivitetType): Iverksett {
        val iverksett = opprettIverksett(behandlingId, null, emptyList(), null)
        val vedtaksperioder = iverksett.vedtak.vedtaksperioder
        return iverksett.copy(vedtak = iverksett.vedtak.copy(vedtaksperioder = vedtaksperioder.map { it.copy(aktivitet = aktivitetType) }))
    }
}