package no.nav.familie.ef.iverksett.brev.aktivitetsplikt

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.aktivitetsplikt.AktivitetspliktInnhentingBrevUtil.opprettBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class AktivitetspliktBrevRepositoryTest : ServerTest() {
    @Autowired
    private lateinit var aktivitetspliktBrevRepository: AktivitetspliktBrevRepository

    @Test
    internal fun `lagring og henting av frittstående brev`() {
        val brev = opprettBrev()

        aktivitetspliktBrevRepository.insert(brev)
        val oppdatertBrev = aktivitetspliktBrevRepository.findByIdOrThrow(brev.id)

        assertThat(oppdatertBrev.id).isEqualTo(brev.id)
        assertThat(oppdatertBrev.personIdent).isEqualTo(brev.personIdent)
        assertThat(oppdatertBrev.oppgaveId).isEqualTo(brev.oppgaveId)
        assertThat(oppdatertBrev.eksternFagsakId).isEqualTo(brev.eksternFagsakId)
        assertThat(oppdatertBrev.journalførendeEnhet).isEqualTo(brev.journalførendeEnhet)
        assertThat(oppdatertBrev.stønadType).isEqualTo(brev.stønadType)
        assertThat(oppdatertBrev.fil).isEqualTo(brev.fil)
        assertThat(oppdatertBrev.journalførendeEnhet).isEqualTo(brev.journalførendeEnhet)
        assertThat(oppdatertBrev.år).isEqualTo(brev.år)
        assertThat(oppdatertBrev.opprettetTid).isEqualTo(brev.opprettetTid)
    }

    @Test
    internal fun `skal oppdatere journalpostId`() {
        val brev = opprettBrev()

        aktivitetspliktBrevRepository.insert(brev)

        val lagretBrev = aktivitetspliktBrevRepository.findByIdOrThrow(brev.id)
        val journalpostId = "journalpostId12345"
        aktivitetspliktBrevRepository.update(lagretBrev.copy(journalpostId = journalpostId))

        val oppdatertBrev = aktivitetspliktBrevRepository.findByIdOrThrow(brev.id)

        assertThat(oppdatertBrev.journalpostId).isEqualTo(journalpostId)
    }

    @Test
    internal fun existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr() {
        val brev = opprettBrev()
        aktivitetspliktBrevRepository.insert(brev)

        assertThat(
            aktivitetspliktBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndÅr(
                brev.eksternFagsakId,
                brev.oppgaveId,
                brev.år,
            ),
        ).isTrue
        assertThat(
            aktivitetspliktBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndÅr(
                brev.eksternFagsakId,
                brev.oppgaveId,
                brev.år.plusYears(1),
            ),
        ).isFalse
    }

    @Test
    internal fun existsByEksternFagsakIdAndGjeldendeÅrAndBrevType() {
        val brev = opprettBrev()
        aktivitetspliktBrevRepository.insert(brev)

        assertThat(
            aktivitetspliktBrevRepository.existsByEksternFagsakIdAndÅr(
                brev.eksternFagsakId,
                brev.år,
            ),
        ).isTrue
        assertThat(
            aktivitetspliktBrevRepository.existsByEksternFagsakIdAndÅr(
                brev.eksternFagsakId,
                brev.år.plusYears(1),
            ),
        ).isFalse
    }
}
