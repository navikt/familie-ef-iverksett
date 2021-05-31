package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.ef.iverksett.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*


class TaskTypeTest {

    @Test
    fun `test taskflyt`() {
        val iverksettMotOppdragTask = Task(IverksettMotOppdragTask.TYPE, "", Properties())
        val ventePåStatusFraØkonomiTask = iverksettMotOppdragTask.opprettNesteTask()

        assertThat(ventePåStatusFraØkonomiTask.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
        assertThat(ventePåStatusFraØkonomiTask.triggerTid).isAfter(LocalDateTime.now().plusMinutes(13))

        val journalførVedtaksbrevTask = ventePåStatusFraØkonomiTask.opprettNesteTask()
        assertThat(journalførVedtaksbrevTask.type).isEqualTo(JournalførVedtaksbrevTask.TYPE)
        assertThat(journalførVedtaksbrevTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val distribuerVedtaksbrevTask = journalførVedtaksbrevTask.opprettNesteTask()
        assertThat(distribuerVedtaksbrevTask.type).isEqualTo(DistribuerVedtaksbrevTask.TYPE)
        assertThat(distribuerVedtaksbrevTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))
    }
}