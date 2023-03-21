package no.nav.familie.ef.iverksett.arena

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("arena-mq")
class ArenaMqConfigProperties(
    val queueManager: String,
    val channel: String,
    val hostName: String,
    val port: String,
    val queueName: String,
    val servicebruker: String,
    val servicebrukerPassord: String,
)
