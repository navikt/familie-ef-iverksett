package no.nav.familie.ef.iverksett.lagreIverksett.tjeneste

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.domene.BehandlingResultat
import no.nav.familie.ef.iverksett.domene.BehandlingType
import no.nav.familie.ef.iverksett.domene.BehandlingÅrsak
import no.nav.familie.ef.iverksett.domene.OpphørÅrsak
import no.nav.familie.ef.iverksett.domene.Vedtak
import no.nav.familie.ef.iverksett.infrastruktur.json.AktivitetskravJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevJson
import no.nav.familie.ef.iverksett.infrastruktur.json.BrevdataJson
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonJson
import no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur.LagreIverksettJdbc
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

class LagreIverksettServiceTest : ServerTest() {

    private lateinit var lagreIverksett: LagreIverksett
    private lateinit var lagreIverksettJdbc: LagreIverksettJdbc
    private lateinit var lagreIverksettService: LagreIverksettService

    @BeforeEach
    fun setUp() {
        lagreIverksett = mockk()
        lagreIverksettJdbc = mockk()
        lagreIverksettService = LagreIverksettService(lagreIverksett)
    }

    @Test
    internal fun `lagre iverksett data og brev`() {
        val behandlingId = UUID.randomUUID()
        val iverksettJson = opprettIverksettJson(behandlingId).toString()

        every { lagreIverksett.lagre(any(), any(), any()) } returns Unit

        lagreIverksettService.lagreIverksettJson(behandlingsId = behandlingId,
                                                 iverksettJson = iverksettJson,
                                                 emptyList())
        verify(exactly = 1) { lagreIverksett.lagre(behandlingId, iverksettJson, emptyList()) }
    }

    private fun opprettIverksettJson(behandlingId: UUID): IverksettJson {
        return IverksettJson(
                brev = listOf(BrevJson(
                        "1", BrevdataJson("mottaker", "saksbehandler")),
                              BrevJson("2", BrevdataJson("mottaker", "saksbehandler"))),
                vedtak = Vedtak.INNVILGET,
                forrigeTilkjentYtelse = emptyList(),
                tilkjentYtelse = emptyList(),
                fagsakId = "1",
                saksnummer = "1",
                behandlingId = behandlingId.toString(),
                eksternId = 1L,
                relatertBehandlingId = "2",
                kode6eller7 = false,
                tidspunktVedtak = OffsetDateTime.now(),
                vilkårsvurderinger = emptyList(),
                person = PersonJson(personIdent = "12345678910", aktorId = null),
                barn = emptyList(),
                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                behandlingResultat = BehandlingResultat.FERDIGSTILT,
                opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
                aktivitetskrav = AktivitetskravJson(LocalDate.now(), false),
                funksjonellId = "0",
                behandlingÅrsak = BehandlingÅrsak.SØKNAD
        )
    }
}