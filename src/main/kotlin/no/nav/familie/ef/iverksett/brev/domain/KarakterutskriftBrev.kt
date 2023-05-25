package no.nav.familie.ef.iverksett.brev.domain

import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
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
    @Column("ar")
    val år: Year,
    val journalpostResultat: JournalpostResultatMap = JournalpostResultatMap(),
    val distribuerBrevResultat: DistribuerBrevResultatMap = DistribuerBrevResultatMap(),
    val opprettetTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
)
