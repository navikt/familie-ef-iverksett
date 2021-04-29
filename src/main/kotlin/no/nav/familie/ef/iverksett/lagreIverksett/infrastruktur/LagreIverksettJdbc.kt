package no.nav.familie.ef.iverksett.lagreIverksett.infrastruktur

import arrow.core.Either
import no.nav.familie.ef.iverksett.domene.Brev
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.KunneIkkeLagreIverksett
import no.nav.familie.ef.iverksett.lagreIverksett.tjeneste.LagreIverksett
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class LagreIverksettJdbc(val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : LagreIverksett {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        val IVERKSETT_JSON_VERSJON: Int = 1
    }

    override fun lagre(iverksettJson: String, brev: List<Brev>): Either<KunneIkkeLagreIverksett, Int> {
        try {
            return Either.Right(lagreIverksett(iverksettJson, brev))
        } catch (exception: Exception) {
            logger.error("Kunne ikke lagre iverksett json : ${exception}")
            return Either.Left(KunneIkkeLagreIverksett)
        }
    }

    private fun lagreIverksett(json: String, brev: List<Brev>): Int {

        val sql = "insert into iverksett values(:uuid, to_json(:json::json), :versjon)"
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        var affectedRows = 0

        namedParameterJdbcTemplate.jdbcTemplate.execute { connection: Connection ->
            connection.autoCommit = false
            var mapSqlParameterSource = MapSqlParameterSource(
                mapOf(
                    "uuid" to UUID.randomUUID(),
                    "json" to json,
                    "versjon" to IVERKSETT_JSON_VERSJON
                )
            )
            val savepoint = connection.setSavepoint()
            try {
                affectedRows = namedParameterJdbcTemplate.update(sql, mapSqlParameterSource, keyHolder)
                val behandlingsId: UUID = keyHolder.keyList[0].get("behandlingid") as UUID
                brev.forEach { lagreBrev(behandlingsId, it) }
                connection.commit()
            } catch (ex: SQLException) {
                connection.rollback(savepoint)
                throw ex
            } finally {
                connection.close()
            }
        }
        return affectedRows
    }

    private fun lagreBrev(behandlingsId: UUID, brev: Brev) {
        val sql = "insert into brev values(:behandlingsId, :journalpostId, :pdf)"
        var mapSqlParameterSource = MapSqlParameterSource(
            mapOf(
                "behandlingsId" to behandlingsId,
                "journalpostId" to brev.journalpostId,
                "pdf" to brev.brevdata.pdf
            )
        )
        val affectedRows = namedParameterJdbcTemplate.update(sql, mapSqlParameterSource)
        if (affectedRows != 1) {
            throw Exception("Kunne ikke legge til brev for behandlingsid : ${behandlingsId})")
        }
    }

}