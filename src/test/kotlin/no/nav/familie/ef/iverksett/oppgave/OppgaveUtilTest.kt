package no.nav.familie.ef.iverksett.oppgave

import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.utledPrioritetForKarakterinnhentingOppgave
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.utledBeskrivelseForKarakterinnhentingOppgave
import no.nav.familie.ef.iverksett.oppgave.OppgaveUtil.utledFristForKarakterinnhentingOppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import java.time.LocalDate

internal class OppgaveUtilTest {

    private val fristHovedperiode = "2023-05-17"
    private val fristUtvidet = "2023-05-18"


    @Nested
    inner class Prioritet {

        @Test
        internal fun `skal utlede riktig prioritet basert på oppgavens tidligere frist`() {
            val prioritetNormal = utledPrioritetForKarakterinnhentingOppgave(fristHovedperiode, 1L)
            val prioritetLav = utledPrioritetForKarakterinnhentingOppgave(fristUtvidet, 1L)

            assertThat(prioritetNormal).isEqualTo(OppgavePrioritet.NORM)
            assertThat(prioritetLav).isEqualTo(OppgavePrioritet.LAV)
        }

        @Test
        internal fun `skal kaste feil dersom ugyldig frist benyttes for å utlede prioritet`() {
            val feil = assertThrows<IllegalStateException> { utledPrioritetForKarakterinnhentingOppgave("2023-01-01", 1L) }

            assertThat(feil.message).contains("Kan ikke oppdatere prioritet på oppgave=")
        }
    }

    @Nested
    inner class Beskrivelse {

        @Test
        internal fun `skal utlede riktig beskrivelse basert på tidligere beskrivelse`() {
            val ingenTidligereBeskrivelse = utledBeskrivelseForKarakterinnhentingOppgave("")
            val tidligereBeskrivelse = utledBeskrivelseForKarakterinnhentingOppgave(tidligereOppgaveBeskrivelse)


            assertThat(ingenTidligereBeskrivelse).isEqualTo(nyttBeskrivelseInnslag)
            assertThat(tidligereBeskrivelse).isEqualTo(nyBeskrivelse)
        }
    }

    @Nested
    inner class Frist {
        @Test
        internal fun `skal utlede riktig frist oppgavens tidligere frist`() {
            val fristFemteAugust = utledFristForKarakterinnhentingOppgave(fristHovedperiode, 1L)
            val fristSjetteAugust = utledFristForKarakterinnhentingOppgave(fristUtvidet, 1L)

            assertThat(fristFemteAugust).isEqualTo(LocalDate.parse("2023-08-05"))
            assertThat(fristSjetteAugust).isEqualTo(LocalDate.parse("2023-08-06"))
        }

        @Test
        internal fun `skal kaste feil dersom ugyldig frist benyttes for å utlede ny frist`() {
            val feil = assertThrows<IllegalStateException> { utledFristForKarakterinnhentingOppgave("2023-01-01", 1L) }

            assertThat(feil.message).contains("Kan ikke oppdatere frist på oppgave=")
        }
    }

    private val nyttBeskrivelseInnslag = "Brev om innhenting av karakterutskrift er sendt ut."

    private val tidligereOppgaveBeskrivelse = "Oppgave opprettet.\n\nOppgave lagt i mappe 64."

    private val nyBeskrivelse = "Brev om innhenting av karakterutskrift er sendt ut.\n\n" +
            "Oppgave opprettet.\n\n" + "Oppgave lagt i mappe 64."
}