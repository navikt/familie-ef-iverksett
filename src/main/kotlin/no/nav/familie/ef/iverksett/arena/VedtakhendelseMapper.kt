package no.nav.familie.ef.iverksett.arena

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.Behandlingstema
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun mapIverkesttTilVedtakHendelser(iverksett: Iverksett, aktørId: String): VedtakHendelser {
    return VedtakHendelser(
            aktoerID = aktørId,
            avslutningsstatus = mapAvslutningsstatus(iverksett.vedtak.vedtaksresultat),
            behandlingstema = Behandlingstema.valueOf(iverksett.fagsak.stønadstype.name.toLowerCase().capitalize()).value,
            hendelsesprodusentREF = "EF",
            applikasjonSakREF = iverksett.fagsak.eksternId.toString(),
            hendelsesTidspunkt = LocalDateTime.now().format(dateTimeFormatter)
    )
}

private fun mapAvslutningsstatus(vedtaksresultat: Vedtaksresultat): String {
    return when (vedtaksresultat) {
        Vedtaksresultat.INNVILGET -> "innvilget"
        Vedtaksresultat.OPPHØRT -> "opphoert"
        else -> error("Håndterer ikke restultat $vedtaksresultat mot arena")
    }
}