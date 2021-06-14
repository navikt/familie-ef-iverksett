package no.nav.familie.ef.iverksett.arena

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "vedtakHendelser")
data class VedtakHendelser(
    val aktoerID: String,
    val avslutningsstatus: String,
    val behandlingstema: String,
    val hendelsesprodusentREF: String,
    val applikasjonSakREF: String,
    val hendelsesTidspunkt: String
)