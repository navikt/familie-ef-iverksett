package no.nav.familie.ef.iverksett.iverksetting.domene

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("iverksett")
data class Iverksett(
    @Id
    val behandlingId: UUID,
    val data: IverksettData,
    val eksternId: Long,
    @MappedCollection(idColumn = "behandling_id")
    val brev: Brev? = null
)
