package no.nav.familie.ef.iverksett.oppgave

import java.util.UUID

data class OppgaveForBarn(val behandlingId: UUID,
                          val beskrivelse: String)

data class OppgaverForBarn(val oppgaverForBarn: List<OppgaveForBarn>)