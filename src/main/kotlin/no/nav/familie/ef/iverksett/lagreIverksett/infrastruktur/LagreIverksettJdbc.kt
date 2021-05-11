package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksett
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.util.*

open class LagreIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreIverksett {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    override fun lagre(behandlingsId: UUID, iverksett: Iverksett, brev: Brev) {
        try {
            return lagreIverksett(behandlingsId, iverksett, brev)
        } catch (exception: Exception) {
            secureLogger.error("Kunne ikke lagre iverksett json for behandlingsId ${behandlingsId}: ${exception}")
            throw Exception("Feil ved LagreIverksett til basen")
        }
    }

    private fun lagreIverksett(behandlingsId: UUID, iverksett: Iverksett, brev: Brev) {
        val sql = "insert into iverksett values(:behandlingsId, :iverksettJson::json)"
        val iverksettString = objectMapper.writeValueAsString(iverksett)

        var mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingsId" to behandlingsId,
                "iverksettJson" to iverksettString
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        lagreBrev(behandlingsId, brev)
    }

    private fun lagreBrev(behandlingsId: UUID, brev: Brev) {
        val sql = "insert into brev values(:behandlingsId, :pdf)"
        var mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingsId" to behandlingsId,
                "pdf" to brev.pdf
            )
        )
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
    }

}