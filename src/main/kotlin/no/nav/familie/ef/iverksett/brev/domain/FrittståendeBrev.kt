package no.nav.familie.ef.iverksett.brev.domain

import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.util.UUID

data class FrittståendeBrev(
    @Id
    val id: UUID = UUID.randomUUID(),
    val personIdent: String,
    val eksternFagsakId: Long,
    @Column("journalforende_enhet")
    val journalførendeEnhet: String,
    val saksbehandlerIdent: String,
    @Column("stonadstype")
    val stønadstype: StønadType,
    val mottakere: Brevmottakere,
    val fil: ByteArray,
    val brevtype: FrittståendeBrevType,
    val journalpostResulat: JournalpostResultatMap = JournalpostResultatMap(),
    val distribuerBrevResulat: DistribuerBrevResultatMap = DistribuerBrevResultatMap()
)