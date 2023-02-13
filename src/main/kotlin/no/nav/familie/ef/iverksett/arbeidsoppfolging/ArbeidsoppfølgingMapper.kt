package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Aktivitetstype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Barn
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Periode
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Periodetype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Stønadstype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.VedtakOvergangsstønadArbeidsoppfølging
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Vedtaksresultat

object ArbeidsoppfølgingMapper {

    fun mapTilVedtakOvergangsstønadTilArbeidsoppfølging(
        iverksett: IverksettOvergangsstønad,
    ): VedtakOvergangsstønadArbeidsoppfølging {
        return VedtakOvergangsstønadArbeidsoppfølging(
            vedtakId = iverksett.behandling.eksternId,
            personIdent = iverksett.søker.personIdent,
            barn = iverksett.søker.barn.map { Barn(it.personIdent, it.termindato) },
            stønadstype = Stønadstype.valueOf(iverksett.fagsak.stønadstype.name),
            vedtaksresultat = Vedtaksresultat.valueOf(iverksett.vedtak.vedtaksresultat.name),
            periode = mapToVedtaksperioder(iverksett.vedtak),
        )
    }

    fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerOvergangsstønad): List<Periode> {
        return vedtaksdetaljer.vedtaksperioder.map {
            Periode(
                it.periode.fomDato,
                it.periode.tomDato,
                Periodetype.valueOf(it.periodeType.name),
                Aktivitetstype.valueOf(it.aktivitet.name),
            )
        }
    }
}
