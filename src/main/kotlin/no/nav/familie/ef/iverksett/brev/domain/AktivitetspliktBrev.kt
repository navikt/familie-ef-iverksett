package no.nav.familie.ef.iverksett.brev.domain

import no.nav.familie.kontrakter.ef.felles.PeriodiskAktivitetspliktBrevDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.Year
import java.time.temporal.ChronoUnit
import java.util.UUID

@Table("aktivitetsplikt_brev")
data class AktivitetspliktBrev(
    @Id
    val id: UUID = UUID.randomUUID(),
    val personIdent: String,
    val oppgaveId: Long,
    val eksternFagsakId: Long,
    @Column("journalforende_enhet")
    val journalførendeEnhet: String,
    val fil: ByteArray,
    @Column("gjeldende_ar")
    val gjeldendeÅr: Year,
    @Column("stonad_type")
    val stønadType: StønadType,
    val journalpostId: String? = null,
    val opprettetTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
)

fun AktivitetspliktBrev.tilDto(): PeriodiskAktivitetspliktBrevDto =
    PeriodiskAktivitetspliktBrevDto(
        fil = this.fil,
        oppgaveId = this.oppgaveId,
        personIdent = this.personIdent,
        eksternFagsakId = this.eksternFagsakId,
        journalførendeEnhet = this.journalførendeEnhet,
        gjeldendeÅr = this.gjeldendeÅr,
        stønadType = this.stønadType,
    )

fun PeriodiskAktivitetspliktBrevDto.tilDomene(journalpostId: String? = null): AktivitetspliktBrev =
    AktivitetspliktBrev(
        fil = this.fil,
        oppgaveId = this.oppgaveId,
        personIdent = this.personIdent,
        eksternFagsakId = this.eksternFagsakId,
        journalførendeEnhet = this.journalførendeEnhet,
        gjeldendeÅr = this.gjeldendeÅr,
        stønadType = this.stønadType,
        journalpostId = journalpostId,
    )
