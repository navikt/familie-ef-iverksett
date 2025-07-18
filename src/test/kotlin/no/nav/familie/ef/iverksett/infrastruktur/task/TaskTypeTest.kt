package no.nav.familie.ef.iverksett.infrastruktur.task

import no.nav.familie.ef.iverksett.arbeidsoppfolging.SendVedtakTilArbeidsoppfølgingTask
import no.nav.familie.ef.iverksett.arena.SendFattetVedtakTilArenaTask
import no.nav.familie.ef.iverksett.behandlingsstatistikk.BehandlingsstatistikkTask
import no.nav.familie.ef.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.familie.ef.iverksett.brev.JournalførVedtaksbrevTask
import no.nav.familie.ef.iverksett.brukernotifikasjon.SendBrukernotifikasjonVedGOmregningTask
import no.nav.familie.ef.iverksett.infotrygd.SendPerioderTilInfotrygdTask
import no.nav.familie.ef.iverksett.oppgave.OpprettFremleggsoppgaverTask
import no.nav.familie.ef.iverksett.oppgave.OpprettOppfølgingsOppgaveForOvergangsstønadTask
import no.nav.familie.ef.iverksett.tilbakekreving.OpprettTilbakekrevingTask
import no.nav.familie.ef.iverksett.vedtak.PubliserVedtakTilKafkaTask
import no.nav.familie.ef.iverksett.vedtakstatistikk.VedtakstatistikkTask
import no.nav.familie.ef.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.familie.ef.iverksett.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.Properties

class TaskTypeTest {
    @Test
    fun `test taskflyt`() {
        val opprettTilbakekrevingTask = Task(OpprettTilbakekrevingTask.TYPE, "", Properties())
        assertThat(opprettTilbakekrevingTask.type).isEqualTo(OpprettTilbakekrevingTask.TYPE)
        assertThat(opprettTilbakekrevingTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val iverksettMotOppdragTask = opprettTilbakekrevingTask.opprettNesteTask()
        assertThat(iverksettMotOppdragTask.type).isEqualTo(IverksettMotOppdragTask.TYPE)
        assertThat(iverksettMotOppdragTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val ventePåStatusFraØkonomiTask = iverksettMotOppdragTask.opprettNesteTask()
        assertThat(ventePåStatusFraØkonomiTask.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
        assertThat(ventePåStatusFraØkonomiTask.triggerTid).isAfter(LocalDateTime.now().plusSeconds(2))

        val journalførVedtaksbrevTask = ventePåStatusFraØkonomiTask.opprettNesteTask()
        assertThat(journalførVedtaksbrevTask.type).isEqualTo(JournalførVedtaksbrevTask.TYPE)
        assertThat(journalførVedtaksbrevTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val distribuerVedtaksbrevTask = journalførVedtaksbrevTask.opprettNesteTask()
        assertThat(distribuerVedtaksbrevTask.type).isEqualTo(DistribuerVedtaksbrevTask.TYPE)
        assertThat(distribuerVedtaksbrevTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))
    }

    @Test
    fun `test publiseringTaskflyt`() {
        val sendPerioderTilInfotrygdTask = Task(SendPerioderTilInfotrygdTask.TYPE, "", Properties())

        assertThat(sendPerioderTilInfotrygdTask.type).isEqualTo(SendPerioderTilInfotrygdTask.TYPE)
        assertThat(sendPerioderTilInfotrygdTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val sendFattetVedtakTilArenaTask = sendPerioderTilInfotrygdTask.opprettNestePubliseringTask()

        assertThat(sendFattetVedtakTilArenaTask.type).isEqualTo(SendFattetVedtakTilArenaTask.TYPE)
        assertThat(sendFattetVedtakTilArenaTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val publiserVedtakTilKafkaTask = sendFattetVedtakTilArenaTask.opprettNestePubliseringTask()
        assertThat(publiserVedtakTilKafkaTask.type).isEqualTo(PubliserVedtakTilKafkaTask.TYPE)
        assertThat(publiserVedtakTilKafkaTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val sendVedtakTilArbeidsoppfølgingTask = publiserVedtakTilKafkaTask.opprettNestePubliseringTask()
        assertThat(sendVedtakTilArbeidsoppfølgingTask.type).isEqualTo(SendVedtakTilArbeidsoppfølgingTask.TYPE)
        assertThat(sendVedtakTilArbeidsoppfølgingTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val opprettOppgaveTask = sendVedtakTilArbeidsoppfølgingTask.opprettNestePubliseringTask()
        assertThat(opprettOppgaveTask.type).isEqualTo(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE)
        assertThat(opprettOppgaveTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val opprettFremleggsOppgaverTask = opprettOppgaveTask.opprettNestePubliseringTask()
        assertThat(opprettFremleggsOppgaverTask.type).isEqualTo(OpprettFremleggsoppgaverTask.TYPE)
        assertThat(opprettFremleggsOppgaverTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val vedtaksstatistikkTask = opprettFremleggsOppgaverTask.opprettNestePubliseringTask()
        assertThat(vedtaksstatistikkTask.type).isEqualTo(VedtakstatistikkTask.TYPE)
        assertThat(vedtaksstatistikkTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val sendBrukernotifikasjonVedGOmregningTask = vedtaksstatistikkTask.opprettNestePubliseringTask()
        assertThat(sendBrukernotifikasjonVedGOmregningTask.type).isEqualTo(SendBrukernotifikasjonVedGOmregningTask.TYPE)
        assertThat(sendBrukernotifikasjonVedGOmregningTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val behandlingsstatistikkTask = sendBrukernotifikasjonVedGOmregningTask.opprettNestePubliseringTask()
        assertThat(behandlingsstatistikkTask.type).isEqualTo(BehandlingsstatistikkTask.TYPE)
        assertThat(behandlingsstatistikkTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))
    }

    @Test
    internal fun `skal ikke opprette task etter opprettet DistribuerVedtaksbrevTask`() {
        val task = Task(DistribuerVedtaksbrevTask.TYPE, "", Properties())
        assertThrows<NoSuchElementException> {
            task.opprettNesteTask()
        }
    }
}
