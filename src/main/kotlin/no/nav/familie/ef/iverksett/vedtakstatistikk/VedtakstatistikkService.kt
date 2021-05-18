package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.domene.Vilkårsresultat
import no.nav.familie.ef.iverksett.økonomi.tilKlassifisering
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingResultat
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.Inntekt
import no.nav.familie.eksterne.kontrakter.ef.Inntektstype
import no.nav.familie.eksterne.kontrakter.ef.PeriodeBeløp
import no.nav.familie.eksterne.kontrakter.ef.Periodetype
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Utbetaling
import no.nav.familie.eksterne.kontrakter.ef.Utbetalingsdetalj
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsvurdering
import no.nav.familie.kontrakter.ef.felles.StønadType
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

     val europeiskZoneId = ZoneId.of("Europe/Paris")

    fun sendTilKafka(iverksett: Iverksett) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett): BehandlingDVH {
        return BehandlingDVH(fagsakId = iverksett.fagsak.fagsakId.toString(),
                             saksnummer = iverksett.fagsak.eksternId.toString(),
                             behandlingId = iverksett.behandling.behandlingId.toString(),
                             relatertBehandlingId = iverksett.behandling.relatertBehandlingId?.toString(),
                             kode6eller7 = iverksett.søker.kode6eller7,
                             tidspunktVedtak = iverksett.vedtak.vedtaksdato.atStartOfDay(europeiskZoneId),
                             vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                             person = mapTilPerson(personIdent = iverksett.søker.personIdent),
                             barn = iverksett.søker.barn.map { mapTilPerson(it.personIdent) },
                             behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
                             behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
                             behandlingResultat = BehandlingResultat.valueOf(iverksett.behandling.behandlingResultat.name),
                             vedtak = Vedtak.valueOf(iverksett.vedtak.vedtak.name),
                             utbetalinger = mapTilUtbetaling(iverksett.vedtak.tilkjentYtelse, iverksett.fagsak.stønadstype, iverksett.fagsak.eksternId, iverksett.søker.personIdent),
                             inntekt = iverksett.vedtak.inntekter.map { inntekt ->
                                 Inntekt(mapTilPeriodeBeløp(inntekt.periodebeløp),
                                         inntekt.inntektstype?.let {Inntektstype.valueOf(it.name) } ?: Inntektstype.ARBEIDINNTEKT)
                             },
                             aktivitetskrav = Aktivitetskrav(iverksett.søker.aktivitetskrav.aktivitetspliktInntreffer,
                                                             iverksett.søker.aktivitetskrav.harSagtOppArbeidsforhold),
                             funksjonellId = iverksett.behandling.eksternId.toString())

    }

    private fun mapTilUtbetaling(tilkjentYtelse: TilkjentYtelse, stønadsType: StønadType, eksternFagsakId: Long, personIdent: String): List<Utbetaling> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(mapTilPeriodeBeløp(it.periodebeløp),
                       Utbetalingsdetalj(gjelderPerson = Person(personIdent),
                                         klassekode = stønadsType.tilKlassifisering(),
                                         delytelseId = eksternFagsakId.toString() + it.periodeId))
        }
    }


    private fun mapTilPeriodeBeløp(periodebeløp: Periodebeløp): PeriodeBeløp {
        return PeriodeBeløp(utbetaltPerPeriode = periodebeløp.beløp,
                            periodetype = Periodetype.valueOf(periodebeløp.periodetype.name),
                            fraOgMed = periodebeløp.fraOgMed,
                            tilOgMed = periodebeløp.tilOgMed)
    }

    private fun mapTilPerson(personIdent: String?, aktorId: String? = null): Person {
        return Person(personIdent = personIdent, aktorId = aktorId)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurdering: no.nav.familie.ef.iverksett.domene.Vilkårsvurdering): Vilkårsvurdering {
        return Vilkårsvurdering(vilkår = Vilkår.valueOf(vilkårsvurdering.vilkårType.name),
                                oppfylt = vilkårsvurdering.resultat == Vilkårsresultat.OPPFYLT)
    }
}