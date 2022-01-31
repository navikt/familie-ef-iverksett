package no.nav.familie.ef.iverksett.oppgave

import java.util.UUID

data class OppgaveForBarn(val behandlingId: UUID,
                          val eksternFagsakId: Long,
                          val personIdent: String,
                          val stønadType: String,
                          val beskrivelse: String)

data class OppgaverForBarn(val oppgaverForBarn: List<OppgaveForBarn>)