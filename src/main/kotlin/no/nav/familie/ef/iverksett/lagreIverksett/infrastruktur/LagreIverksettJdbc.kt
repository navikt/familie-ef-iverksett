package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksett
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.util.*

open class LagreIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreIverksett {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    override fun lagre(behandlingsId: UUID, iverksettJson: String, brev: List<Brev>) {
        try {
            return lagreIverksett(behandlingsId, iverksettJson, brev)
        } catch (exception: Exception) {
            secureLogger.error("Kunne ikke lagre iverksett json for behandlingsId ${behandlingsId}: ${exception}")
            throw Exception("Feil ved LagreIverksett til basen")
        }
    }

    private fun lagreIverksett(behandlingsId: UUID, iverksettJson: String, brev: List<Brev>) {
        val sql = "insert into iverksett values(:behandlingsId, :iverksettJson::json)"
        var mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingsId" to behandlingsId,
                "iverksettJson" to iverksettJson
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        brev.forEach { lagreBrev(behandlingsId, it) }
    }

    private fun lagreBrev(behandlingsId: UUID, brev: Brev) {
        val sql = "insert into brev values(:behandlingsId, :journalpostId, :pdf)"
        var mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingsId" to behandlingsId,
                "journalpostId" to brev.journalpostId,
                "pdf" to brev.brevdata.pdf
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

}