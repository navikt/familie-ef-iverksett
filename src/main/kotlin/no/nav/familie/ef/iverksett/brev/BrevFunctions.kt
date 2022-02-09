package no.nav.familie.ef.iverksett.brev

import no.nav.familie.kontrakter.ef.felles.FrittståendeBrevType
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype

fun frittståendeBrevtypeTilDokumenttype(frittståendeBrevType: FrittståendeBrevType) =
        when (frittståendeBrevType) {
            FrittståendeBrevType.INFOBREV_OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV
            FrittståendeBrevType.MANGELBREV_OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV
            FrittståendeBrevType.INFOBREV_BARNETILSYN -> Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV
            FrittståendeBrevType.MANGELBREV_BARNETILSYN -> Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV
            FrittståendeBrevType.INFOBREV_SKOLEPENGER -> Dokumenttype.SKOLEPENGER_FRITTSTÅENDE_BREV
            FrittståendeBrevType.MANGELBREV_SKOLEPENGER -> Dokumenttype.SKOLEPENGER_FRITTSTÅENDE_BREV
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

fun lagVedtakstekst(vedtaksresultat: Vedtaksresultat): String =
        when (vedtaksresultat) {
            Vedtaksresultat.INNVILGET -> "Vedtak om innvilgelse av "
            Vedtaksresultat.AVSLÅTT -> "Vedtak om avslag av "
            Vedtaksresultat.OPPHØRT -> "Vedtak om opphør av "
        }