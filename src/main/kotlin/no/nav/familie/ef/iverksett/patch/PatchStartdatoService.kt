package no.nav.familie.ef.iverksett.patch

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@Unprotected
@RequestMapping("api/patch-startdato")
class PatchStartdatoController(private val patchStartdatoService: PatchStartdatoService) {

    @PostMapping
    fun patch(@RequestBody data: List<String>,
              @RequestParam oppdaterVedtak: Boolean = false) {
        patchStartdatoService.patch(data, oppdaterVedtak)
    }
}

@Service
class PatchStartdatoService(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun patch(data: List<String>, oppdaterVedtak: Boolean) {
        if (data.isEmpty()) {
            logger.warn("Mangler data")
            return
        }
        data.map { line -> line.split(",").let { UUID.fromString(it[0]) to it[1] } }.forEach { (behandlingId, startdato) ->
            try {
                patch(behandlingId, startdato, oppdaterVedtak)
            } catch (e: Exception) {
                throw RuntimeException("Feilet patching av $behandlingId", e)
            }
        }
    }

    fun patch(behandlingId: UUID, startdato: String, oppdaterVedtak: Boolean) {
        LocalDate.parse(startdato) // kun for å sjekke att dato er gyldig
        val harOppdatert = oppdaterIverksett(behandlingId, startdato, oppdaterVedtak)
        if (!harOppdatert) {
            logger.info("behandling=$behandlingId har ikke blitt oppdatert, då den ikke finnes")
            return
        }
        oppdaterIverksettResultat(behandlingId, startdato, oppdaterVedtak)
    }

    private fun oppdaterIverksett(behandlingId: UUID, startdato: String, oppdaterVedtak: Boolean): Boolean {
        val params = mapOf("behandlingId" to behandlingId,
                           "type" to IverksettType.VANLIG.name)
        val sql = """SELECT data FROM iverksett WHERE behandling_id = :behandlingId AND type = :type"""
        val dataJson = namedParameterJdbcTemplate.query(sql, MapSqlParameterSource(params)) { rs, _ ->
            rs.getString("data")
        }.singleOrNull() ?: return false

        val iverksett = objectMapper.readTree(dataJson)
        (iverksett.get("vedtak").get("tilkjentYtelse") as ObjectNode).put("startdato", startdato)
        val mapSqlParameterSource = MapSqlParameterSource(params.toMutableMap().apply {
            put("data", objectMapper.writeValueAsString(iverksett))
        })
        if (oppdaterVedtak) {
            val updateSql = """UPDATE iverksett SET data = :data::JSON WHERE behandling_id = :behandlingId AND type= :type"""
            namedParameterJdbcTemplate.update(updateSql, mapSqlParameterSource)
        }
        return true
    }

    private fun oppdaterIverksettResultat(behandlingId: UUID, startdato: String, oppdaterVedtak: Boolean) {
        val params = mapOf<String, Any>("behandlingId" to behandlingId)
        val sql = """SELECT tilkjentytelseforutbetaling FROM iverksett_resultat WHERE behandling_id = :behandlingId"""
        val dataJson =
                namedParameterJdbcTemplate.queryForObject(sql, MapSqlParameterSource(params)) { rs, _ ->
                    rs.getString("tilkjentYtelseForUtbetaling")
                }
        if (dataJson == null) {
            logger.warn("behandling=$behandlingId finner ikke iverksett_resultat")
            return
        }
        val iverksett = objectMapper.readTree(dataJson) as ObjectNode
        iverksett.put("startdato", startdato)

        val mapSqlParameterSource = MapSqlParameterSource(params.toMutableMap().apply {
            put("data", objectMapper.writeValueAsString(iverksett))
        })
        if (oppdaterVedtak) {
            val updateSql =
                    """UPDATE iverksett_resultat SET tilkjentytelseforutbetaling = :data::JSON WHERE behandling_id = :behandlingId"""
            namedParameterJdbcTemplate.update(updateSql, mapSqlParameterSource)
        }
    }
}
