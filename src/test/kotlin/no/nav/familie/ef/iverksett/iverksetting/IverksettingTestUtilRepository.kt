package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettType
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class IverksettingTestUtilRepository(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    @Transactional
    fun oppdaterData(): Int {
        val sql = """
            UPDATE iverksett SET data =
            (select regexp_replace(data::text, '(.*)(vedtaksdato":")(\d+-\d+-\d+)(.*)','\1vedtakstidspunkt":"\3T00:00:00\4')::json from iverksett)
                """
        return namedParameterJdbcTemplate.update(sql, MapSqlParameterSource())
    }


    @Transactional
    fun manueltLagreIverksett(behandlingId: UUID, iverksettJson: String) {
        val sql = "INSERT INTO iverksett VALUES(:behandlingId, :iverksettJson::JSON, :type, :eksternId)"

        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "iverksettJson" to iverksettJson,
                "type" to IverksettType.VANLIG.name,
                "eksternId" to 1L
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

}