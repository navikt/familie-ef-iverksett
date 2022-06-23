package no.nav.familie.ef.iverksett.brev

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.ef.StønadType

fun stønadstypeTilDokumenttype(stønadType: StønadType) =
    when (stønadType) {
        StønadType.OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV
        StønadType.SKOLEPENGER -> Dokumenttype.SKOLEPENGER_FRITTSTÅENDE_BREV
        StønadType.BARNETILSYN -> Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV
    }

fun vedtaksbrevForStønadType(stønadType: StønadType): Dokumenttype =
    when (stønadType) {
        StønadType.OVERGANGSSTØNAD -> Dokumenttype.VEDTAKSBREV_OVERGANGSSTØNAD
        StønadType.BARNETILSYN -> Dokumenttype.VEDTAKSBREV_BARNETILSYN
        StønadType.SKOLEPENGER -> Dokumenttype.VEDTAKSBREV_SKOLEPENGER
    }

fun lagStønadtypeTekst(stønadstype: StønadType): String =
    when (stønadstype) {
        StønadType.OVERGANGSSTØNAD -> "overgangstønad"
        StønadType.BARNETILSYN -> "stønad til barnetilsyn"
        StønadType.SKOLEPENGER -> "stønad til skolepenger"
    }

fun lagVedtakstekst(iverksett: Iverksett): String =
    when {
        iverksett.behandling.behandlingType === BehandlingType.FØRSTEGANGSBEHANDLING ->
            lagVedtakstekstFørstegangsbehandling(iverksett)
        iverksett.behandling.behandlingÅrsak === BehandlingÅrsak.SANKSJON_1_MND -> "Vedtak om sanksjon av "
        iverksett.vedtak.vedtaksresultat === Vedtaksresultat.AVSLÅTT -> "Vedtak om avslått "
        iverksett.vedtak.vedtaksresultat === Vedtaksresultat.OPPHØRT -> "Vedtak om opphørt "
        iverksett.vedtak.vedtaksresultat === Vedtaksresultat.INNVILGET &&
            iverksett.behandling.behandlingÅrsak === BehandlingÅrsak.SØKNAD -> "Vedtak om innvilget "
        else -> "Vedtak om revurdert "
    }

private fun lagVedtakstekstFørstegangsbehandling(iverksett: Iverksett) =
    when (iverksett.vedtak.vedtaksresultat) {
        Vedtaksresultat.INNVILGET -> "Vedtak om innvilget "
        Vedtaksresultat.AVSLÅTT -> "Vedtak om avslått "
        Vedtaksresultat.OPPHØRT -> error("Førstegangsbehandling kan ikke ha resultat Opphørt")
    }
