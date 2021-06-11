package no.nav.familie.ef.iverksett.arena

data class VedtakHendelser(
    val aktoerID: String,
    val avslutningsstatus: String,
    val behandlingstema: String,
    val hendelsesprodusentREF: String,
    val applikasjonSakREF: String,
    val hendelsesTidspunkt: String
)