package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettType
import no.nav.familie.ef.iverksett.util.getUUID
import no.nav.familie.ef.iverksett.util.queryForJson
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.UUID

@Repository
class IverksettingRepository(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    @Transactional
    fun lagre(behandlingId: UUID, iverksett: Iverksett, brev: Brev?) {
        lagreIverksett(behandlingId, iverksett)
        brev?.let { lagreBrev(behandlingId, it) }
    }

    private fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett) {
        val sql = "INSERT INTO iverksett VALUES(:behandlingId, :iverksettJson::JSON, :type, :eksternId)"
        val iverksettString = objectMapper.writeValueAsString(iverksett)
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "iverksettJson" to iverksettString,
                "type" to IverksettType.VANLIG.name,
                "eksternId" to iverksett.behandling.eksternId
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

    private fun lagreBrev(behandlingId: UUID, brev: Brev) {
        val sql = "INSERT INTO brev VALUES(:behandlingId, :pdf)"
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "pdf" to brev.pdf
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

    fun hent(behandlingId: UUID): Iverksett {
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingId" to behandlingId,
                "type" to IverksettType.VANLIG.name
            )
        )
        return namedParameterJdbcTemplate.queryForJson(HENT_IVERKSETT_SQL, mapSqlParameterSource)
            ?: error("Finner ikke iverksett med behandlingId=$behandlingId")
    }

    fun hentAvEksternId(eksternId: Long): Iverksett {
        val mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "eksternId" to eksternId,
                "type" to IverksettType.VANLIG.name
            )
        )
        return namedParameterJdbcTemplate.queryForJson(HENT_IVERKSETT_EKSTERN_ID_SQL, mapSqlParameterSource)
            ?: error("Finner ikke iverksett med eksternId=$eksternId")
    }

    fun hentBrev(behandlingId: UUID): Brev {
        val sql = "SELECT * FROM brev WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { resultSet: ResultSet, _: Int ->
            val behandlingIdForBrev = resultSet.getUUID("behandling_id")
            val pdf = resultSet.getBytes("pdf")
            Brev(behandlingIdForBrev, pdf)
        } ?: error("Fant ikke brev for behandlingId : $behandlingId")
    }

    // language=PostgreSQL
    companion object {

        const val HENT_IVERKSETT_SQL = "SELECT data FROM iverksett WHERE behandling_id = :behandlingId AND type = :type"
        const val HENT_IVERKSETT_EKSTERN_ID_SQL = "SELECT data FROM iverksett WHERE ekstern_id = :eksternId AND type = :type"
    }
}
