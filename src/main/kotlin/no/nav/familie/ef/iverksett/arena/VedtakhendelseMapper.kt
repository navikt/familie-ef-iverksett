package no.nav.familie.ef.iverksett.arena

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.Behandlingstema
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun mapIverksettTilVedtakHendelser(
    iverksettData: IverksettData,
    fodselsnr: String,
): VedtakHendelser =
    VedtakHendelser(
        fodselsnr = fodselsnr,
        avslutningsstatus = mapAvslutningsstatus(iverksettData.vedtak.vedtaksresultat),
        behandlingstema =
            Behandlingstema
                .valueOf(
                    iverksettData.fagsak.stønadstype.name
                        .lowercase(Locale.getDefault())
                        .replaceFirstChar { it.uppercase() },
                ).value,
        hendelsesprodusentREF = "EF",
        applikasjonSakREF = iverksettData.fagsak.eksternId.toString(),
        hendelsesTidspunkt = LocalDateTime.now().format(dateTimeFormatter),
    )

private fun mapAvslutningsstatus(vedtaksresultat: Vedtaksresultat): String =
    when (vedtaksresultat) {
        Vedtaksresultat.INNVILGET -> "innvilget"
        Vedtaksresultat.OPPHØRT -> "opphoert"
        else -> error("Håndterer ikke restultat $vedtaksresultat mot arena")
    }
