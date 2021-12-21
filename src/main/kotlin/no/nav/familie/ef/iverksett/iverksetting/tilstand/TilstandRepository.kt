package no.nav.familie.ef.iverksett.iverksetting.tilstand

import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.util.SporbarUtils
import no.nav.familie.ef.iverksett.util.getJson
import no.nav.familie.ef.iverksett.util.getUUID
import no.nav.familie.ef.iverksett.util.queryForJson
import no.nav.familie.ef.iverksett.util.queryForNullableObject
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.UUID

@Repository
class TilstandRepository(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun opprettTomtResultat(behandlingId: UUID) {
        val sql = "INSERT INTO iverksett_resultat VALUES(:behandlingId, NULL, NULL, NULL, NULL, NULL)"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

    fun oppdaterTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        oppdaterKolonne(behandlingId, "tilkjentytelseforutbetaling", tilkjentYtelseForUtbetaling)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        oppdaterKolonne(behandlingId, "oppdragresultat", oppdragResultat)
    }


    fun oppdaterJournalpostResultat(behandlingId: UUID, journalPostResultat: JournalpostResultat) {
        oppdaterKolonne(behandlingId, "journalpostresultat", journalPostResultat)
    }

    fun oppdaterDistribuerVedtaksbrevResultat(behandlingId: UUID, distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat) {
        oppdaterKolonne(behandlingId, "vedtaksbrevresultat", distribuerVedtaksbrevResultat)
    }

    fun oppdaterTilbakekrevingResultat(behandlingId: UUID, tilbakekrevingResultat: TilbakekrevingResultat) {
        oppdaterKolonne(behandlingId, "tilbakekrevingresultat", tilbakekrevingResultat)
    }

    private fun oppdaterKolonne(behandlingId: UUID, kolonne: String, obj: Any) {
        val sql = "UPDATE iverksett_resultat SET $kolonne = :jsonString::JSON, " +
                  "endret_tid = :endretTid " +
                  "WHERE behandling_id = :behandlingId"
        val jsonString = objectMapper.writeValueAsString(obj)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
                .addValue("jsonString", jsonString)
                .addValue("endretTid", SporbarUtils.now())

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
        ?: error("Iverksett resultat ble ikke oppdatert for behandling=$behandlingId, " +
                 "mangler sansynligvis et resultat for behandlingen")
    }


    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        val sql = "SELECT tilkjentytelseforutbetaling FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
    }

    fun hentTilkjentYtelse(behandlingId: Set<UUID>): Map<UUID, TilkjentYtelse> {
        val sql =
                "SELECT behandling_id, tilkjentytelseforutbetaling FROM iverksett_resultat WHERE behandling_id IN (:behandlingId)"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        val resultSetExtractor = ResultSetExtractor { rs ->
            val result = mutableMapOf<UUID, TilkjentYtelse>()
            while (rs.next()) {
                result[rs.getUUID("behandling_id")] =
                        rs.getJson("tilkjentytelseforutbetaling")!!
            }
            result.toMap()
        }
        val result = namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, resultSetExtractor)!!
        if (!result.keys.containsAll(behandlingId)) {
            val behandlingIdnSomSavnerMatchIResult = behandlingId.toMutableSet()
            behandlingIdnSomSavnerMatchIResult.removeAll(result.keys)
            error("Finner ikke tilkjent ytelse til behandlingIdn=$behandlingIdnSomSavnerMatchIResult")
        }
        return result
    }

    fun hentJournalpostResultat(behandlingId: UUID): JournalpostResultat? {
        val sql = "SELECT journalpostresultat FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
    }

    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat? {
        val sql = "SELECT * FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForNullableObject(sql, mapSqlParameterSource) { rs: ResultSet, _: Int ->
            IverksettResultat(
                    UUID.fromString(rs.getString("behandling_id")),
                    rs.getJson("tilkjentYtelseForUtbetaling"),
                    rs.getJson("oppdragResultat"),
                    rs.getJson("journalpostResultat"),
                    rs.getJson("vedtaksBrevResultat"),
                    rs.getJson("tilbakekrevingResultat"))
        }
    }

    fun hentTilbakekrevingResultat(behandlingId: UUID): TilbakekrevingResultat? {
        val sql = "SELECT tilbakekrevingresultat FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
    }
}