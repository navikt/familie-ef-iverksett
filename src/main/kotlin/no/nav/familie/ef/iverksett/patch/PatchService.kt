package no.nav.familie.ef.iverksett.patch

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettType
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Unprotected
@RequestMapping("api/patch-aktivitet")
class PatchController(private val patchService: PatchService) {

    @GetMapping
    fun patch(@RequestParam oppdaterVedtak: Boolean = false) {
        patchService.patch(oppdaterVedtak)
    }
}

@Service
class PatchService(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val behandlingIder = setOf<String>()
    fun patch(oppdaterVedtak: Boolean) {
        behandlingIder.map { UUID.fromString(it) }.forEach {
            patch(it, oppdaterVedtak)
        }
    }

    fun patch(behandlingId: UUID, oppdaterVedtak: Boolean) {
        val params = mapOf("behandlingId" to behandlingId,
                           "type" to IverksettType.VANLIG.name)
        val dataJson =
                namedParameterJdbcTemplate.queryForObject("""SELECT data FROM iverksett WHERE behandling_id = :behandlingId AND type = :type""",
                                                          MapSqlParameterSource(params)) { rs, _ ->
                    rs.getString("data")
                } ?: error("Finner ikke data for behandling=$behandlingId")
        val iverksett = objectMapper.readValue<Iverksett>(dataJson)

        if (objectMapper.readTree(objectMapper.writeValueAsString(iverksett)) != objectMapper.readTree(dataJson)) {
            logger.info("Json er ulik for behandling=$behandlingId")
            return
        }
        val vedtaksperioder = iverksett.vedtak.vedtaksperioder
        if (vedtaksperioder.size != 1) {
            logger.info("Vedtaksperioder har size=${vedtaksperioder.size} behandling=$behandlingId")
            return
        }
        val aktivitet = vedtaksperioder.single().aktivitet
        if (aktivitet != AktivitetType.MIGRERING) {
            logger.info("aktivitet=$aktivitet for behandling=$behandlingId")
            return
        }
        if (oppdaterVedtak) {
            val oppdatertIverksett =
                    iverksett.copy(vedtak = iverksett.vedtak.copy(vedtaksperioder = vedtaksperioder.map { it.copy(aktivitet = AktivitetType.FORSØRGER_REELL_ARBEIDSSØKER) }))
            val mapSqlParameterSource = MapSqlParameterSource(params.toMutableMap().apply {
                put("data", objectMapper.writeValueAsString(oppdatertIverksett))
            })
            namedParameterJdbcTemplate.update("""UPDATE iverksett SET data=:data::JSON WHERE behandling_id = :behandlingId AND type = :type""",
                                              mapSqlParameterSource)
        }
    }
}
