package no.nav.familie.ef.iverksett.iverksetting.tilstand

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.util.getUUID
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.NULL_DATO
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("api/tilstandpatch")
@Unprotected
class TilstandPatchController(private val tilstandPatch: TilstandPatch) {

    @GetMapping
    fun oppdaterSisteAndelIKjede(@RequestParam oppdaterDatabas: Boolean = false) {
        tilstandPatch.oppdaterSisteAndelIKjede(oppdaterDatabas)
    }
}

@Service
class TilstandPatch(private val jdbcTemplate: JdbcTemplate,
                    private val tilstandRepository: TilstandRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun oppdaterSisteAndelIKjede(oppdaterDatabas: Boolean = false) {
        val data = getData()
        data.groupBy { it.fagsakId }.values.forEach { gruppe ->
            håndterGruppe(gruppe)
        }
    }

    fun håndterGruppe(gruppe: List<TilstandPatchData>, oppdaterDatabas: Boolean = true) {
        var tidligereAndel: AndelTilkjentYtelse? = null
        gruppe.sortedBy { it.tilkjentYtelse.utbetalingsoppdrag!!.avstemmingTidspunkt }.forEach { data ->
            val behandlingId = data.behandlingId
            val tilkjentYtelse = data.tilkjentYtelse
            val sisteAndel =
                    tilkjentYtelse.andelerTilkjentYtelse.maxByOrNull { it.fraOgMed }?.takeIf { it.fraOgMed != NULL_DATO }
            val sisteAndelPeriodeId = tidligereAndel?.periodeId
            val tidligerePeriodeId = sisteAndel?.periodeId

            if (tidligereAndel == null && sisteAndel == null) {
                logger.warn("fagsak=${data.fagsakId} behandling=$behandlingId - finner ikke andel siste andel")
                return
            }
            if (tidligereAndel == null || (sisteAndelPeriodeId != null && tidligerePeriodeId != null && sisteAndelPeriodeId < tidligerePeriodeId)) {
                tidligereAndel = sisteAndel
            }

            logger.info("fagsak=${data.fagsakId} behandling=$behandlingId - setter periodeId=${tidligereAndel?.periodeId}")
            if (oppdaterDatabas) {
                tilstandRepository.oppdaterTilkjentYtelseForUtbetaling(behandlingId,
                                                                       tilkjentYtelse.copy(sisteAndelIKjede = tidligereAndel))
            }
        }
    }

    private fun getData(): List<TilstandPatchData> {
        val sql = """
            SELECT ir.behandling_id, i.data -> 'fagsak' ->> 'fagsakId' fagsak_id, tilkjentytelseforutbetaling 
            FROM iverksett i
            JOIN iverksett_resultat ir ON i.behandling_id = ir.behandling_id
            WHERE tilkjentytelseforutbetaling IS NOT NULL
            AND i.type = 'VANLIG'
            """
        val data = jdbcTemplate.query(sql) { rs, _ ->
            val behandlingId = rs.getUUID("behandling_id")
            val fagsakId = rs.getUUID("fagsak_id")
            val tilkjentYtelse = rs.getString("tilkjentytelseforutbetaling")
            Triple(fagsakId, behandlingId, tilkjentYtelse)
        }
        return data.mapNotNull {
            val fagsakId = it.first
            val behandlingId = it.second
            val tilkjentYtelseJsonString = it.third
            val tilkjentYtelse = objectMapper.readValue<TilkjentYtelse>(tilkjentYtelseJsonString)
            val json2 = objectMapper.writeValueAsString(tilkjentYtelse)
            if (tilkjentYtelseJsonString != json2) {
                logger.warn("Behandling=$behandlingId sin json er ikke lik den serialiserte json")
                return@mapNotNull null
            }
            if (tilkjentYtelse.utbetalingsoppdrag == null) {
                logger.warn("Behandling=$behandlingId mangler utbetalingsoppdrag")
                return@mapNotNull null
            }
            TilstandPatchData(fagsakId, behandlingId, tilkjentYtelse)
        }
    }

    class TilstandPatchData(val fagsakId: UUID, val behandlingId: UUID, val tilkjentYtelse: TilkjentYtelse)
}