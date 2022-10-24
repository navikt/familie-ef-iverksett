package no.nav.familie.ef.iverksett.brev.domain

import java.time.LocalDateTime

/**
 * @param [JournalpostResultat] per ident
 */
data class JournalpostResultatMap(val map: Map<String, JournalpostResultat> = emptyMap()) {

    operator fun plus(tillegg: Map<String, JournalpostResultat>): JournalpostResultatMap =
        JournalpostResultatMap(this.map + tillegg)

    fun isNotEmpty() = map.isNotEmpty()
}

data class JournalpostResultat(
    val journalpostId: String,
    val journalført: LocalDateTime = LocalDateTime.now()
)
