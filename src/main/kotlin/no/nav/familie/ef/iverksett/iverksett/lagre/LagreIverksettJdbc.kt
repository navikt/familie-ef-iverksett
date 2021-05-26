package no.nav.familie.ef.iverksett.iverksett.lagre

import no.nav.familie.ef.iverksett.iverksett.Brev
import no.nav.familie.ef.iverksett.iverksett.Iverksett
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class LagreIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    fun lagre(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        try {
            return lagreIverksett(behandlingId, iverksett, brev)
        } catch (exception: Exception) {
            secureLogger.error("Kunne ikke lagre iverksett json for behandlingId ${behandlingId}", exception)
            throw Exception("Feil ved LagreIverksett til basen")
        }
    }

    private fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        val sql = "insert into iverksett values(:behandlingId, :iverksettJson::json)"
        val iverksettString = objectMapper.writeValueAsString(iverksett)

        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                    "behandlingId" to behandlingId,
                    "iverksettJson" to iverksettString
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        lagreBrev(behandlingId, brev)
    }

    private fun lagreBrev(behandlingId: UUID, brev: Brev) {
        val sql = "insert into brev values(:behandlingId, :pdf)"
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "pdf" to brev.pdf
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

}