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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@Unprotected
@RequestMapping("api/patch-iverksett")
class PatchFagsakController(private val patchFagsakService: PatchFagsakService) {

    @GetMapping
    fun patch(@RequestParam dry: Boolean = true) {
        patchFagsakService.patchIverksetting(dry, data)
    }
}

@Service
class PatchFagsakService(
        private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun patchIverksetting(dry: Boolean, data: List<PatchFagsakData>) {
        data.forEach {
            val behandlingId = it.behandlingId
            val params = mapOf("behandlingId" to behandlingId,
                               "type" to IverksettType.VANLIG.name)
            val sql = """SELECT data FROM iverksett WHERE behandling_id = :behandlingId AND type = :type"""
            val json = namedParameterJdbcTemplate.queryForObject(sql, MapSqlParameterSource(params)) { rs, _ ->
                rs.getString("data")
            } ?: error("Finner ikke data for behandling=$behandlingId")

            val iverksett = objectMapper.readTree(json)

            val fagsakJson = iverksett.get("fagsak")
            val fagsak = fagsakJson.toString()

            (fagsakJson as ObjectNode).put("eksternId", it.fagsakEksternId)
            fagsakJson.put("fagsakId", it.fagsakId.toString())

            logger.info("Oppdaterer behandling=$behandlingId med $it med tidligere data $fagsak ny data $fagsakJson")

            if (!dry) {
                val mapSqlParameterSource = MapSqlParameterSource(params.toMutableMap().apply {
                    put("data", objectMapper.writeValueAsString(iverksett))
                })
                val updateSql = """UPDATE iverksett SET data=:data::JSON WHERE behandling_id = :behandlingId AND type = :type"""
                namedParameterJdbcTemplate.update(updateSql, mapSqlParameterSource)
            }

        }
    }
}

data class PatchFagsakData(val fagsakEksternId: Long, val fagsakId: UUID, val behandlingId: UUID, val behandlingEksternId: Long)

val data = listOf(PatchFagsakData(3353,
                                  UUID.fromString("1e98b81a-ffd9-4f18-b54d-2eb125cacff6"),
                                  UUID.fromString("52a66467-c05b-4345-aaea-f2294a0ae681"),
                                  4359),
                  PatchFagsakData(4379,
                                  UUID.fromString("2227b15e-741b-471c-beab-7ab636c9cab7"),
                                  UUID.fromString("e7641995-82fd-40cb-98fb-2b5bc101d5f5"),
                                  4379),
                  PatchFagsakData(3228,
                                  UUID.fromString("223cd010-dcd4-470d-922b-47f5592324c2"),
                                  UUID.fromString("bc90d099-0f64-437c-9dd8-fa9f9f5dcc7b"),
                                  4178),
                  PatchFagsakData(4399,
                                  UUID.fromString("23a72970-f6b0-47ba-953e-054fd670d07b"),
                                  UUID.fromString("afa32de0-f6ad-4486-a574-42ebc51d78f1"),
                                  4399),
                  PatchFagsakData(3283,
                                  UUID.fromString("241f34ca-e92d-4610-b1d5-fc78afd0acc8"),
                                  UUID.fromString("94211fca-0690-4807-865f-bb6f4e0fb3d9"),
                                  4253),
                  PatchFagsakData(4216,
                                  UUID.fromString("27fb48ef-e80d-4ffc-8cae-cabbeeca56cc"),
                                  UUID.fromString("d947b053-033c-4995-868d-8c93e4be7505"),
                                  4216),
                  PatchFagsakData(4407,
                                  UUID.fromString("2db3f40d-446d-41ca-bbac-978c4501101b"),
                                  UUID.fromString("b057ff2e-edf4-4a87-9f48-38c68e6f17b5"),
                                  4407),
                  PatchFagsakData(1401,
                                  UUID.fromString("3435f44c-ec11-4768-8e9e-bd5ca1d8a5ab"),
                                  UUID.fromString("87133a7d-e869-4f42-ab5b-db3f9783889e"),
                                  1589),
                  PatchFagsakData(4393,
                                  UUID.fromString("3963d6cd-68d3-4ce6-b0e2-e1b6e49577fa"),
                                  UUID.fromString("a108cd32-63ef-43c3-953f-9bc4a3009c8f"),
                                  4393),
                  PatchFagsakData(4273,
                                  UUID.fromString("4d743168-4fda-4440-9a9e-8b0c6689f3e4"),
                                  UUID.fromString("3f731532-06c0-4dda-89da-914d2c3e1517"),
                                  4273),
                  PatchFagsakData(3368,
                                  UUID.fromString("5d1a72d9-9234-4b0a-8251-d1fa33594b71"),
                                  UUID.fromString("896e5e1e-d41d-4f09-802c-d230a0dfd4bc"),
                                  4371),
                  PatchFagsakData(4388,
                                  UUID.fromString("6283d075-febf-4d0c-873d-681dd60818ac"),
                                  UUID.fromString("98d0c69b-542a-4c79-aa3e-65d99e79e13e"),
                                  4388),
                  PatchFagsakData(4386,
                                  UUID.fromString("63ed1ecc-45fe-41c5-828a-063d23ceaeea"),
                                  UUID.fromString("0e4f76eb-1103-4847-962c-72dc73ecb38d"),
                                  4386),
                  PatchFagsakData(4403,
                                  UUID.fromString("64789382-c57f-46e5-a03e-6135bb24a944"),
                                  UUID.fromString("7e3618cf-9e97-468a-bc1d-863676c78414"),
                                  4403),
                  PatchFagsakData(3364,
                                  UUID.fromString("70198ca1-83dc-42f4-92e0-00027f8e9bf3"),
                                  UUID.fromString("93b8ada3-8be6-451a-b2f1-76a4973900c5"),
                                  4396),
                  PatchFagsakData(3302,
                                  UUID.fromString("760aec73-0853-403b-8fa6-e2cd5bcfa1b4"),
                                  UUID.fromString("0eceeb8f-c396-491f-a41a-f4e2d0258ded"),
                                  4278),
                  PatchFagsakData(3336,
                                  UUID.fromString("7756dc40-6d60-4857-a966-895ebefe900a"),
                                  UUID.fromString("8e59f338-1796-479b-938a-af43b6372765"),
                                  4330),
                  PatchFagsakData(4401,
                                  UUID.fromString("8810884c-c813-4d89-b09b-7cad227ff136"),
                                  UUID.fromString("c0ad1056-43e7-4f48-ba31-7cf244080401"),
                                  4401),
                  PatchFagsakData(3341,
                                  UUID.fromString("8c09c154-208d-4a13-a335-f76c75435e11"),
                                  UUID.fromString("21f322de-c824-4432-8eba-0a554cd4b7bd"),
                                  4336),
                  PatchFagsakData(4409,
                                  UUID.fromString("8c534a19-975d-4b9a-b3c3-bc2e0bbe9067"),
                                  UUID.fromString("dcd4ce01-5d64-4e26-8fd2-b5d0aeba80a7"),
                                  4409),
                  PatchFagsakData(4397,
                                  UUID.fromString("9a938982-a4be-4de6-8a79-877ac70bc7d4"),
                                  UUID.fromString("d92824cb-71a7-491a-8124-3c8b0dfdd52d"),
                                  4397),
                  PatchFagsakData(4408,
                                  UUID.fromString("b2ad32ea-acdd-4eca-b7fc-084ec4130a1b"),
                                  UUID.fromString("68ff60b3-64a1-47d0-a426-7972c737abf4"),
                                  4408),
                  PatchFagsakData(3319,
                                  UUID.fromString("c9706b9f-2eeb-4c29-8c1c-43d9de48b7a6"),
                                  UUID.fromString("09ee9b86-caa0-4c2c-bb48-2891f91906a7"),
                                  4303),
                  PatchFagsakData(4384,
                                  UUID.fromString("ce41e938-b755-4e79-abcb-089fbe080bb3"),
                                  UUID.fromString("8d3fd8f9-d469-4f01-b3c1-34d0779b4162"),
                                  4384),
                  PatchFagsakData(4405,
                                  UUID.fromString("d9142a37-c9d7-4173-b77e-28cebc1bf757"),
                                  UUID.fromString("021dd233-b4bb-46f2-a0f7-102f7379ac36"),
                                  4405),
                  PatchFagsakData(2473,
                                  UUID.fromString("dcd46f43-cdc7-4275-9f70-270fc4d50509"),
                                  UUID.fromString("faed9b5e-fc38-4afc-a307-396c063a455d"),
                                  2473),
                  PatchFagsakData(2770,
                                  UUID.fromString("dd0c6f9a-40ab-4f0f-8acc-c7d16283779a"),
                                  UUID.fromString("c6ba223c-5dbf-43a6-a74b-ef87169b98e4"),
                                  3533),
                  PatchFagsakData(4383,
                                  UUID.fromString("eb22ff59-12c8-4b9c-88d1-9d4efd8463b8"),
                                  UUID.fromString("fbb4865f-3620-4675-a059-004d8673140b"),
                                  4383),
                  PatchFagsakData(2927,
                                  UUID.fromString("f3025911-25e6-45f6-b09d-d38a349c948a"),
                                  UUID.fromString("fb755d0a-377a-4a0b-8a6d-0804c84925a2"),
                                  3754))