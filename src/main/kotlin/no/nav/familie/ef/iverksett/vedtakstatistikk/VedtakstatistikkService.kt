package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.domene.Iverksett
import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.TilkjentYtelseMedMetaData
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
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(iverksett: Iverksett) {
        val vedtakstatistikk = hentBehandlingDVH(iverksett)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksett: Iverksett): BehandlingDVH {
        return BehandlingDVH(fagsakId = iverksett.fagsakId,
                             saksnummer = iverksett.saksnummer,
                             behandlingId = iverksett.behandlingId,
                             relatertBehandlingId = iverksett.relatertBehandlingId,
                             kode6eller7 = iverksett.kode6eller7,
                             tidspunktVedtak = iverksett.tidspunktVedtak?.atStartOfDay(ZoneId.of("Europe/Paris")),
                             vilkårsvurderinger = iverksett.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                             person = mapTilPerson(iverksett.personIdent),
                             barn = iverksett.barn.map { mapTilPerson(it.personIdent, it.aktorId) },
                             behandlingType = BehandlingType.valueOf(iverksett.behandlingType.name),
                             behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandlingÅrsak.name),
                             behandlingResultat = BehandlingResultat.valueOf(iverksett.behandlingResultat.name),
                             vedtak = iverksett.vedtak?.let { Vedtak.valueOf(it.name) },
                             utbetalinger = mapTilUtbetaling(iverksett.tilkjentYtelse),
                             inntekt = iverksett.inntekt.map {
                                 Inntekt(mapTilPeriodeBeløp(it.periodebeløp),
                                         Inntektstype.valueOf(it.inntektstype.name))
                             },
                             aktivitetskrav = Aktivitetskrav(iverksett.aktivitetskrav.aktivitetspliktInntreffer,
                                                             iverksett.aktivitetskrav.harSagtOppArbeidsforhold),
                             funksjonellId = iverksett.funksjonellId)

    }

    private fun mapTilUtbetaling(tilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData): List<Utbetaling> {
        return tilkjentYtelseMedMetaData.tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(mapTilPeriodeBeløp(it.periodebeløp),
                       Utbetalingsdetalj(gjelderPerson = Person(it.personIdent),
                                         klassekode = it.stønadsType!!.tilKlassifisering(),
                                         delytelseId = tilkjentYtelseMedMetaData.eksternFagsakId.toString() + it.periodeId))
        }
    }


    private fun mapTilPeriodeBeløp(periodebeløp: Periodebeløp): PeriodeBeløp {
        return PeriodeBeløp(utbetaltPerPeriode = periodebeløp.utbetaltPerPeriode,
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