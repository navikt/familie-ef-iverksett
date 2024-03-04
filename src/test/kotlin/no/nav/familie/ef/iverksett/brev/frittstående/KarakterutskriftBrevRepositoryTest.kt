package no.nav.familie.ef.iverksett.brev.frittstående

import no.nav.familie.ef.iverksett.ServerTest
import no.nav.familie.ef.iverksett.brev.frittstående.KarakterInnhentingBrevUtil.opprettBrev
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class KarakterutskriftBrevRepositoryTest : ServerTest() {
    @Autowired
    private lateinit var karakterutskriftBrevRepository: KarakterutskriftBrevRepository

    @Test
    internal fun `lagring og henting av frittstående brev`() {
        val brev = opprettBrev(brevType = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE)

        karakterutskriftBrevRepository.insert(brev)
        val oppdatertBrev = karakterutskriftBrevRepository.findByIdOrThrow(brev.id)

        assertThat(oppdatertBrev.id).isEqualTo(brev.id)
        assertThat(oppdatertBrev.personIdent).isEqualTo(brev.personIdent)
        assertThat(oppdatertBrev.oppgaveId).isEqualTo(brev.oppgaveId)
        assertThat(oppdatertBrev.eksternFagsakId).isEqualTo(brev.eksternFagsakId)
        assertThat(oppdatertBrev.journalførendeEnhet).isEqualTo(brev.journalførendeEnhet)
        assertThat(oppdatertBrev.brevtype).isEqualTo(brev.brevtype)
        assertThat(oppdatertBrev.stønadType).isEqualTo(brev.stønadType)
        assertThat(oppdatertBrev.fil).isEqualTo(brev.fil)
        assertThat(oppdatertBrev.brevtype).isEqualTo(brev.brevtype)
        assertThat(oppdatertBrev.journalførendeEnhet).isEqualTo(brev.journalførendeEnhet)
        assertThat(oppdatertBrev.gjeldendeÅr).isEqualTo(brev.gjeldendeÅr)
        assertThat(oppdatertBrev.opprettetTid).isEqualTo(brev.opprettetTid)
    }

    @Test
    internal fun `skal oppdatere journalpostId`() {
        val brev = opprettBrev(brevType = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE)

        karakterutskriftBrevRepository.insert(brev)

        val lagretBrev = karakterutskriftBrevRepository.findByIdOrThrow(brev.id)
        val journalpostId = "journalpostId12345"
        karakterutskriftBrevRepository.update(lagretBrev.copy(journalpostId = journalpostId))

        val oppdatertBrev = karakterutskriftBrevRepository.findByIdOrThrow(brev.id)

        assertThat(oppdatertBrev.journalpostId).isEqualTo(journalpostId)
    }

    @Test
    internal fun existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr() {
        val brev = opprettBrev(brevType = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE)
        karakterutskriftBrevRepository.insert(brev)

        assertThat(
            karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(
                brev.eksternFagsakId,
                brev.oppgaveId,
                brev.gjeldendeÅr,
            ),
        ).isTrue
        assertThat(
            karakterutskriftBrevRepository.existsByEksternFagsakIdAndOppgaveIdAndGjeldendeÅr(
                brev.eksternFagsakId,
                brev.oppgaveId,
                brev.gjeldendeÅr.plusYears(1),
            ),
        ).isFalse
    }

    @Test
    internal fun existsByEksternFagsakIdAndGjeldendeÅrAndBrevType() {
        val brev = opprettBrev(brevType = FrittståendeBrevType.INNHENTING_AV_KARAKTERUTSKRIFT_HOVEDPERIODE)
        karakterutskriftBrevRepository.insert(brev)

        assertThat(
            karakterutskriftBrevRepository.existsByEksternFagsakIdAndGjeldendeÅrAndBrevtype(
                brev.eksternFagsakId,
                brev.gjeldendeÅr,
                brev.brevtype,
            ),
        ).isTrue
        assertThat(
            karakterutskriftBrevRepository.existsByEksternFagsakIdAndGjeldendeÅrAndBrevtype(
                brev.eksternFagsakId,
                brev.gjeldendeÅr.plusYears(1),
                brev.brevtype,
            ),
        ).isFalse
    }
}
