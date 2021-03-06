package no.nav.familie.ef.iverksett.iverksetting.tilstand

import no.nav.familie.ef.iverksett.iverksetting.domene.DistribuerVedtaksbrevResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.JournalpostResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
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

        val sql = "UPDATE iverksett_resultat SET tilkjentytelseforutbetaling = :tilkjentYtelseForUtbetaling::JSON " +
            "WHERE behandling_id = :behandlingId"
        val tilkjentYtelseForUtbetalingJson = objectMapper.writeValueAsString(tilkjentYtelseForUtbetaling)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("tilkjentYtelseForUtbetaling", tilkjentYtelseForUtbetalingJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error("Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = $behandlingId")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        val sql = "UPDATE iverksett_resultat SET oppdragresultat = :oppdragResultat::JSON WHERE behandling_id = :behandlingId"

        val oppdragResultatJson = objectMapper.writeValueAsString(oppdragResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("oppdragResultat", oppdragResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error(
                "Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = $behandlingId, " +
                    "oppdragResultatJson : $oppdragResultatJson"
            )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterJournalpostResultat(behandlingId: UUID, mottakerIdent: String, journalPostResultat: JournalpostResultat) {
        val sql = "UPDATE iverksett_resultat SET journalpostresultat = :journalpostResultat::JSON " +
            "WHERE behandling_id = :behandlingId"

        val eksisterendeResultat = hentJournalpostResultat(behandlingId)
        val resultatermedNyBrevmottaker = (eksisterendeResultat ?: emptyMap()) + mapOf(mottakerIdent to journalPostResultat)

        val journalPostResultatJson = objectMapper.writeValueAsString(resultatermedNyBrevmottaker)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("journalpostResultat", journalPostResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error(
                "Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = $behandlingId," +
                    "journalPostResultatJson : $journalPostResultatJson"
            )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterDistribuerVedtaksbrevResultat(
        behandlingId: UUID,
        journalpostId: String,
        distribuerVedtaksbrevResultat: DistribuerVedtaksbrevResultat
    ) {
        val sql = "UPDATE iverksett_resultat SET vedtaksbrevresultat = :distribuerVedtaksbrevResultatBrevmottakereJson::JSON " +
            "WHERE behandling_id = :behandlingId"

        val eksisterendeResultat: Map<String, DistribuerVedtaksbrevResultat>? = hentdistribuerVedtaksbrevResultat(behandlingId)

        val resultatermedNyJournalpost = (eksisterendeResultat ?: emptyMap()) + mapOf(journalpostId to distribuerVedtaksbrevResultat)

        val distribuerVedtaksbrevResultatBrevmottakereJson = objectMapper.writeValueAsString(resultatermedNyJournalpost)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("distribuerVedtaksbrevResultatBrevmottakereJson", distribuerVedtaksbrevResultatBrevmottakereJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error(
                "Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = $behandlingId, " +
                    "distribuerVedtaksbrevResultatBrevmottakereJson : $distribuerVedtaksbrevResultatBrevmottakereJson"
            )
    }

    fun hentdistribuerVedtaksbrevResultat(behandlingId: UUID): Map<String, DistribuerVedtaksbrevResultat>? {
        val sql = "SELECT vedtaksbrevresultat FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
    }

    fun oppdaterTilbakekrevingResultat(
        behandlingId: UUID,
        tilbakekrevingResultat: TilbakekrevingResultat
    ) {
        val sql = "UPDATE iverksett_resultat SET tilbakekrevingresultat = :tilbakekrevingResultatJson::JSON " +
            "WHERE behandling_id = :behandlingId"
        val tilbakekrevingResultatJson = objectMapper.writeValueAsString(tilbakekrevingResultat)
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
            .addValue("tilbakekrevingResultatJson", tilbakekrevingResultatJson)

        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource).takeIf { it == 1 }
            ?: error(
                "Kunne ikke oppdatere tabell. Skyldes trolig feil behandlingId = $behandlingId, " +
                    "tilbakekrevingResultatJson : $tilbakekrevingResultatJson"
            )
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

    fun hentJournalpostResultat(behandlingId: UUID): Map<String, JournalpostResultat>? {
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
                rs.getJson("tilbakekrevingResultat")
            )
        }
    }

    fun hentTilbakekrevingResultat(behandlingId: UUID): TilbakekrevingResultat? {
        val sql = "SELECT tilbakekrevingresultat FROM iverksett_resultat WHERE behandling_id = :behandlingId"
        val mapSqlParameterSource = MapSqlParameterSource("behandlingId", behandlingId)
        return namedParameterJdbcTemplate.queryForJson(sql, mapSqlParameterSource)
    }
}
