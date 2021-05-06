package no.nav.familie.ef.iverksett.vedtakstatistikk

import no.nav.familie.ef.iverksett.domene.Vilkårsresultat
import no.nav.familie.ef.iverksett.infrastruktur.json.IverksettJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PeriodebeløpJson
import no.nav.familie.ef.iverksett.infrastruktur.json.PersonJson
import no.nav.familie.ef.iverksett.infrastruktur.json.VilkårsvurderingJson
import no.nav.familie.eksterne.kontrakter.ef.Aktivitetskrav
import no.nav.familie.eksterne.kontrakter.ef.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.ef.BehandlingResultat
import no.nav.familie.eksterne.kontrakter.ef.BehandlingType
import no.nav.familie.eksterne.kontrakter.ef.BehandlingÅrsak
import no.nav.familie.eksterne.kontrakter.ef.PeriodeBeløp
import no.nav.familie.eksterne.kontrakter.ef.Periodetype
import no.nav.familie.eksterne.kontrakter.ef.Person
import no.nav.familie.eksterne.kontrakter.ef.Vedtak
import no.nav.familie.eksterne.kontrakter.ef.Vilkår
import no.nav.familie.eksterne.kontrakter.ef.Vilkårsvurdering
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer) {

    fun sendTilKafka(iverksettJson: IverksettJson) {
        val vedtakstatistikk = mapTilBehandlingDVH(iverksettJson)
        vedtakstatistikkKafkaProducer.sendVedtak("")
    }

    private fun mapTilBehandlingDVH(iverksettJson: IverksettJson): BehandlingDVH {
        return BehandlingDVH(fagsakId = iverksettJson.fagsakId,
                             saksnummer = iverksettJson.saksnummer,
                             behandlingId = iverksettJson.behandlingId,
                             relatertBehandlingId = iverksettJson.relatertBehandlingId,
                             kode6eller7 = iverksettJson.kode6eller7,
                //  tidspunktVedtak = iverksettJson.tidspunktVedtak, //ZoneDateTime
                             vilkårsvurderinger = iverksettJson.vilkårsvurderinger.map { mapTilVilkårsvurderinger(it) },
                             person = mapTilPerson(iverksettJson.person),
                             barn = iverksettJson.barn.map { mapTilPerson(it) },
                             behandlingType = BehandlingType.valueOf(iverksettJson.behandlingType.name),
                             behandlingÅrsak = BehandlingÅrsak.valueOf(iverksettJson.behandlingÅrsak.name),
                             behandlingResultat = BehandlingResultat.valueOf(iverksettJson.behandlingResultat.name),
                             vedtak = iverksettJson.vedtak?.let { Vedtak.valueOf(it.name) },
                // iverksettJson, //utbetaling
                //  iverksettJson,//inntekter,
                             aktivitetskrav = Aktivitetskrav(iverksettJson.aktivitetskrav.aktivitetspliktInntrefferDato,
                                                             iverksettJson.aktivitetskrav.harSagtOppArbeidsforhold),
                             funksjonellId = iverksettJson.funksjonellId)

    }

    private fun mapTilUtbetaling(til) {
        return listOf()
    }

    private fun mapTilPeriodeBeløp(periodebeløpJson: PeriodebeløpJson): PeriodeBeløp {
        return PeriodeBeløp(utbetaltPerPeriode = periodebeløpJson.utbetaltPerPeriode,
                            periodetype = Periodetype.valueOf(periodebeløpJson.periodetype.name),
                            fraOgMed = periodebeløpJson.fraOgMed,
                            tilOgMed = periodebeløpJson.tilOgMed)
    }

    private fun mapTilPerson(person: PersonJson): Person {
        return Person(personIdent = person.personIdent, aktorId = person.aktorId)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurderingJson: VilkårsvurderingJson): Vilkårsvurdering {
        return Vilkårsvurdering(vilkår = Vilkår.valueOf(vilkårsvurderingJson.vilkårType.name),
                                oppfylt = vilkårsvurderingJson.resultat == Vilkårsresultat.OPPFYLT)
    }


}