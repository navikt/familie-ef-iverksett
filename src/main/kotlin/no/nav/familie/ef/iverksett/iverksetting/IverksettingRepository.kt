package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
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
    fun lagre(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        return lagreIverksett(behandlingId, iverksett, brev)
    }

    private fun lagreIverksett(behandlingId: UUID, iverksett: Iverksett, brev: Brev) {
        val sql = "INSERT INTO iverksett VALUES(:behandlingId, :iverksettJson::JSON)"
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
        return hentIverksettStringOgTransformer(behandlingId)
    }

    fun hentBrev(behandlingId: UUID): Brev {
        val sql = "SELECT * FROM brev WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForObject(sql, mapSqlParameterSource) { resultSet: ResultSet, _: Int ->
            val behandlingIdForBrev = resultSet.getUUID("behandling_id")
            val pdf = resultSet.getBytes("pdf")
            Brev(behandlingIdForBrev, pdf)
        } ?: error("Fant ikke brev for behandlingId : ${behandlingId}")
    }

    private fun hentIverksettStringOgTransformer(behandlingId: UUID): Iverksett {

        val sql = "select data from iverksett where behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
               ?: error("Finner ikke iverksett med behandlingId=${behandlingId}")
    }


}