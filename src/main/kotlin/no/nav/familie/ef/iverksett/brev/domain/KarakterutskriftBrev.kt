package no.nav.familie.ef.iverksett.brev.domain

import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.ef.felles.KarakterutskriftBrevDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.Year
import java.time.temporal.ChronoUnit
import java.util.UUID

@Table("karakterutskrift_brev")
data class KarakterutskriftBrev(
    @Id
    val id: UUID = UUID.randomUUID(),
    val personIdent: String,
    val oppgaveId: Long,
    val eksternFagsakId: Long,
    @Column("journalforende_enhet")
    val journalførendeEnhet: String,
    val fil: ByteArray,
    val brevtype: FrittståendeBrevType,
    @Column("gjeldende_ar")
    val gjeldendeÅr: Year,
    @Column("stonad_type")
    val stønadType: StønadType,
    val journalpostId: String? = null,
    val opprettetTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
)

fun KarakterutskriftBrev.tilDto(): KarakterutskriftBrevDto = KarakterutskriftBrevDto(
    fil = this.fil,
    oppgaveId = this.oppgaveId,
    personIdent = this.personIdent,
    eksternFagsakId = this.eksternFagsakId,
    journalførendeEnhet = this.journalførendeEnhet,
    brevtype = this.brevtype,
    gjeldendeÅr = this.gjeldendeÅr,
    stønadType = this.stønadType,
)

fun KarakterutskriftBrevDto.tilDomene(): KarakterutskriftBrev = KarakterutskriftBrev(
    fil = this.fil,
    oppgaveId = this.oppgaveId,
    personIdent = this.personIdent,
    eksternFagsakId = this.eksternFagsakId,
    journalførendeEnhet = this.journalførendeEnhet,
    brevtype = this.brevtype,
    gjeldendeÅr = this.gjeldendeÅr,
    stønadType = this.stønadType,
)
