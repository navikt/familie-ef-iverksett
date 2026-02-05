package no.nav.familie.ef.iverksett.arbeidsoppfolging

import no.nav.familie.ef.iverksett.util.lagMånedsperiode
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Aktivitetstype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Barn
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Periode
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Periodetype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Stønadstype
import no.nav.familie.eksterne.kontrakter.arbeidsoppfolging.Vedtaksresultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class ArbeidsoppfølgingMapperTest {
    @Test
    fun mapTilVedtakOvergangsstønadTilArbeidsoppfølging() {
        val iverksett = opprettIverksettOvergangsstønad(UUID.randomUUID())

        val vedtakTilArbeidsoppfølging = ArbeidsoppfølgingMapper.mapTilVedtakOvergangsstønadTilArbeidsoppfølging(iverksett)

        assertThat(vedtakTilArbeidsoppfølging.vedtakId).isEqualTo(iverksett.behandling.eksternId)
        assertThat(vedtakTilArbeidsoppfølging.stønadstype).isEqualTo(Stønadstype.OVERGANGSSTØNAD)
        assertThat(vedtakTilArbeidsoppfølging.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(vedtakTilArbeidsoppfølging.vedtaksresultat).isEqualTo(Vedtaksresultat.INNVILGET)
        assertThat(vedtakTilArbeidsoppfølging.personIdent).isEqualTo(iverksett.søker.personIdent)

        val forventetPeriode =
            Periode(
                lagMånedsperiode(YearMonth.now()).fomDato,
                lagMånedsperiode(YearMonth.now()).tomDato,
                Periodetype.HOVEDPERIODE,
                Aktivitetstype.BARNET_ER_SYKT,
            )

        assertThat(vedtakTilArbeidsoppfølging.periode).isEqualTo(listOf(forventetPeriode))

        val forventetBarnList = listOf(Barn("01010199999"), Barn(null, LocalDate.of(2023, 1, 1)))
        assertThat(vedtakTilArbeidsoppfølging.barn).isEqualTo(forventetBarnList)
    }
}
