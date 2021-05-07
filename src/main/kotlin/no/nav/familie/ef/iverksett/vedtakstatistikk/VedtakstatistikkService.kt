package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.domene.Periodebeløp
import no.nav.familie.ef.iverksett.domene.Vilkårsresultat
import no.nav.familie.ef.iverksett.infrastruktur.json.AndeltilkjentYtelseJson
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonJson
import no.nav.familie.ef.iverksett.infrastruktur.json.VilkårsvurderingJson
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

    fun sendTilKafka(iverksettJson: IverksettJson) {
        val vedtakstatistikk = hentBehandlingDVH(iverksettJson)
        vedtakstatistikkKafkaProducer.sendVedtak(vedtakstatistikk)
    }

    private fun hentBehandlingDVH(iverksettJson: IverksettJson): BehandlingDVH {
        return BehandlingDVH(fagsakId = iverksettJson.fagsakId,
                             saksnummer = iverksettJson.saksnummer,
                             behandlingId = iverksettJson.behandlingId,
                             relatertBehandlingId = iverksettJson.relatertBehandlingId,
                             kode6eller7 = iverksettJson.kode6eller7,
                             tidspunktVedtak = iverksettJson.tidspunktVedtak?.atStartOfDay(ZoneId.of("Europe/Paris")), //?
                             vilkårsvurderinger = iverksettJson.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                             person = mapTilPerson(iverksettJson.person),
                             barn = iverksettJson.barn.map { mapTilPerson(it) },
                             behandlingType = BehandlingType.valueOf(iverksettJson.behandlingType.name),
                             behandlingÅrsak = BehandlingÅrsak.valueOf(iverksettJson.behandlingÅrsak.name),
                             behandlingResultat = BehandlingResultat.valueOf(iverksettJson.behandlingResultat.name),
                             vedtak = iverksettJson.vedtak?.let { Vedtak.valueOf(it.name) },
                             utbetalinger = iverksettJson.tilkjentYtelse.map {
                                 mapTilUtbetaling(it, iverksettJson.saksnummer)
                             },
                            //TODO implementasjon av inntekt i sak mangler
                             inntekt = iverksettJson.tilkjentYtelse.map {
                                 Inntekt(mapTilPeriodeBeløp(it.inntektbeløp),
                                         Inntektstype.valueOf(it.inntektstype.name))
                             },
                             aktivitetskrav = Aktivitetskrav(iverksettJson.aktivitetskrav.aktivitetspliktInntrefferDato,
                                                             iverksettJson.aktivitetskrav.harSagtOppArbeidsforhold),
                             funksjonellId = iverksettJson.funksjonellId)

    }

    private fun mapTilUtbetaling(andeltilkjentYtelseJson: AndeltilkjentYtelseJson, saksnummer: String?): Utbetaling {
        return Utbetaling(mapTilPeriodeBeløp(andeltilkjentYtelseJson.periodebeløp),
                          Utbetalingsdetalj(gjelderPerson = Person(andeltilkjentYtelseJson.personIdent),
                                            klassekode = andeltilkjentYtelseJson.stønadsType.tilKlassifisering(),
                                            delytelseId = saksnummer + andeltilkjentYtelseJson.periodeId))
    }


    private fun mapTilPeriodeBeløp(periodebeløp: Periodebeløp): PeriodeBeløp {
        return PeriodeBeløp(utbetaltPerPeriode = periodebeløp.utbetaltPerPeriode,
                            periodetype = Periodetype.valueOf(periodebeløp.periodetype.name),
                            fraOgMed = periodebeløp.fraOgMed,
                            tilOgMed = periodebeløp.tilOgMed)
    }

    private fun mapTilPerson(person: PersonJson): Person {
        return Person(personIdent = person.personIdent, aktorId = person.aktorId)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurderingJson: VilkårsvurderingJson): Vilkårsvurdering {
        return Vilkårsvurdering(vilkår = Vilkår.valueOf(vilkårsvurderingJson.vilkårType.name),
                                oppfylt = vilkårsvurderingJson.resultat == Vilkårsresultat.OPPFYLT)
    }
}