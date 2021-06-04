package no.nav.familie.ef.iverksett.arena

import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.melding.virksomhet.vedtakhendelser.v1.vedtakhendelser.WSApplikasjoner
import no.nav.melding.virksomhet.vedtakhendelser.v1.vedtakhendelser.WSAvslutningsstatuser
import no.nav.melding.virksomhet.vedtakhendelser.v1.vedtakhendelser.WSBehandlingstema
import no.nav.melding.virksomhet.vedtakhendelser.v1.vedtakhendelser.WSVedtakHendelser
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.datatype.DatatypeFactory


fun mapIverksettTilVedtakHendelser(iverksett: Iverksett): WSVedtakHendelser {
    val wsVedtakHendelser = WSVedtakHendelser()

    val wsAvslutningsstatuser = WSAvslutningsstatuser()
    wsAvslutningsstatuser.kodeRef = iverksett.vedtak.vedtaksresultat.name
    wsVedtakHendelser.avslutningsstatus = wsAvslutningsstatuser //Arena støtter ikke avslått?

    val wsBehandlingstema = WSBehandlingstema()
    wsBehandlingstema.value = "ab0272" //Enslig forsørger
    wsVedtakHendelser.behandlingstema = wsBehandlingstema

    val wsApplikasjoner = WSApplikasjoner()
    wsApplikasjoner.value = "EF"
    wsVedtakHendelser.hendelsesprodusentREF = wsApplikasjoner

    wsVedtakHendelser.aktoerID = iverksett.søker.personIdent
    wsVedtakHendelser.applikasjonSakREF = iverksett.fagsak.eksternId.toString()
    wsVedtakHendelser.hendelsesTidspunkt = DatatypeFactory.newInstance().newXMLGregorianCalendar(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date())
    )

    return wsVedtakHendelser
}