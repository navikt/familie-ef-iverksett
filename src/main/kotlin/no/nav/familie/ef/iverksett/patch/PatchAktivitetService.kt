package no.nav.familie.ef.iverksett.patch

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettType
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Unprotected
@RequestMapping("api/patch-aktivitet")
class PatchAktivitetController(private val patchAktivitetService: PatchAktivitetService) {

    @PostMapping
    fun patch(@RequestBody data: List<String>,
              @RequestParam oppdaterVedtak: Boolean = false) {
        patchAktivitetService.patch(data, oppdaterVedtak)
    }
}

@Service
class PatchAktivitetService(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun patch(data: List<String>, oppdaterVedtak: Boolean) {
        data.map { UUID.fromString(it) }.forEach {
            patch(it, oppdaterVedtak)
        }
    }

    fun patch(behandlingId: UUID, oppdaterVedtak: Boolean) {
        val params = mapOf("behandlingId" to behandlingId,
                           "type" to IverksettType.VANLIG.name)
        val sql = """SELECT data FROM iverksett WHERE behandling_id = :behandlingId AND type = :type"""
        val json = namedParameterJdbcTemplate.queryForObject(sql, MapSqlParameterSource(params)) { rs, _ ->
            rs.getString("data")
        } ?: error("Finner ikke data for behandling=$behandlingId")

        val iverksett = objectMapper.readTree(json)

        val vedtaksperioder = iverksett.get("vedtak").get("vedtaksperioder")
        if (vedtaksperioder.size() != 1) {
            logger.info("Vedtaksperioder har size=${vedtaksperioder.size()} behandling=$behandlingId")
            return
        }

        val periode = vedtaksperioder.single()
        val aktivitet = periode.get("aktivitet").asText().let { AktivitetType.valueOf(it) }
        if (aktivitet != AktivitetType.MIGRERING) {
            logger.info("aktivitet=$aktivitet for behandling=$behandlingId")
            return
        }
        (periode as ObjectNode).put("aktivitet", AktivitetType.FORSØRGER_REELL_ARBEIDSSØKER.name)
        if (oppdaterVedtak) {
            val mapSqlParameterSource = MapSqlParameterSource(params.toMutableMap().apply {
                put("data", objectMapper.writeValueAsString(iverksett))
            })
            val updateSql = """UPDATE iverksett SET data=:data::JSON WHERE behandling_id = :behandlingId AND type = :type"""
            namedParameterJdbcTemplate.update(updateSql, mapSqlParameterSource)
        }
    }
}
