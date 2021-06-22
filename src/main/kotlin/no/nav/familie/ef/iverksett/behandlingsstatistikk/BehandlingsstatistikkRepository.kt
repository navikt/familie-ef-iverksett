package no.nav.familie.ef.iverksett.behandlingsstatistikk

import no.nav.familie.ef.iverksett.util.queryForJson
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.ef.iverksett.Hendelse
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BehandlingsstatistikkRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagre(behandlingId: UUID, behandlingDVH: BehandlingDVH, hendelse: Hendelse) {
        return lagreBehandlingstatistikk(behandlingId, behandlingDVH, hendelse)
    }

    fun hent(behandlingId: UUID, hendelse: Hendelse): BehandlingDVH {
        val sql = "SELECT behandling_dvh FROM behandling_statistikk WHERE behandling_id = :behandlingId AND hendelse= :hendelse "
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        mapSqlParameterSource.addValue("hendelse", hendelse.toString())
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
               ?: error("Ingen treff på behandling_id=${behandlingId} med hendelse=${hendelse.name}")
    }

    private fun lagreBehandlingstatistikk(behandlingId: UUID, behandlingDVH: BehandlingDVH, hendelse: Hendelse) {
        val sql =
                "INSERT INTO behandling_statistikk VALUES(:behandlingId, :behandlingDVH::JSON, :hendelse)"
        val behandlingDVHString = objectMapper.writeValueAsString(behandlingDVH)

        val mapSqlParameterSource = MapSqlParameterSource(
                mapOf(
                        "behandlingId" to behandlingId,
                        "behandlingDVH" to behandlingDVHString,
                        "hendelse" to hendelse.name
                )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }
}