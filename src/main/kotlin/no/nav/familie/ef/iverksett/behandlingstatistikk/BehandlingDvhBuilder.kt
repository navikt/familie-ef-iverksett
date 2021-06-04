package no.nav.familie.ef.iverksett.behandlingstatistikk

import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Verdier som foreløbig er hardkodet inntil mere er på plass
 */
val sakYtelse = "EFOG"
val behandlingMetode = "MANUELL"
val avsender = "NAV Enslig forelder"
val behandlingType = "Førstegangsbehandling"
val opprettetEnhet = "" // TODO : Finne ut av denne
val ansvarligEnhet = "" // TODO : Finne ut av denne

class BehandlingDvhBuilder private constructor(

        val behandlingUuid: String,
        val relatertBehandlingId: String,
        val sakId: String,
        val mottattTid: ZonedDateTime,
        val ferdigBehandletTid: ZonedDateTime,
        val vedtakTid: ZonedDateTime,
        val datoForsteUtbetaling: LocalDate,
        val sakUtland: String,
        val behandlingResultat: String,
        val resultatBegrunnelse: String,
        val ansvarligBeslutter: String,
        val versjon: String) {

    data class Builder(
            var behandlingUuid: String? = null,
            var relatertBehandlingId: String? = null,
            var sakId: String? = null,
            var mottattTid: ZonedDateTime? = null,
            var ferdigBehandletTid: ZonedDateTime? = null,
            var vedtakTid: ZonedDateTime? = null,
            var datoForsteUtbetaling: LocalDate? = null,
            var sakUtland: String? = null,
            var venteAarsak: String? = null,
            var behandlingResultat: String? = null,
            var resultatBegrunnelse: String? = null,
            var ansvarligBeslutter: String? = null,
            var versjon: String? = null
    ) {

        fun behandlingUuid(behandlingUuid: String?) {
            apply { this.behandlingUuid = behandlingUuid }
        }

        fun relatertBehandlingId(relatertBehandlingId: String?) {
            apply { this.relatertBehandlingId = relatertBehandlingId }
        }

        fun mottattTid(mottattTid: ZonedDateTime?) {
            apply { this.mottattTid = mottattTid }
        }

        fun ferdigBehandletTid(ferdigBehandletTid: ZonedDateTime?) {
            apply { this.ferdigBehandletTid = ferdigBehandletTid }
        }

        fun vedtakTid(vedtakTid: ZonedDateTime?) {
            apply { this.vedtakTid = vedtakTid }
        }

        fun datoForsteUtbetaling(datoForsteUtbetaling: LocalDate?) {
            apply { this.datoForsteUtbetaling = datoForsteUtbetaling }
        }

        fun sakUtland(sakUtland: String?) {
            apply { this.sakUtland = sakUtland }
        }

        fun venteAarsak(venteAarsak: String?) {
            apply { this.venteAarsak = venteAarsak }
        }

        fun behandlingResultat(behandlingResultat: String?) {
            apply { this.behandlingResultat = behandlingResultat }
        }

        fun resultatBegrunnelse(resultatBegrunnelse: String?) {
            apply { this.resultatBegrunnelse = resultatBegrunnelse }
        }

        fun ansvarligBeslutter(ansvarligBeslutter: String?) {
            apply { this.ansvarligBeslutter = ansvarligBeslutter }
        }

        fun versjon(versjon: String?) {
            apply { this.versjon = versjon }
        }


        fun build(
                behandlingId: String,
                aktorId: String,
                registrertTid: ZonedDateTime,
                endretTid: ZonedDateTime,
                tekniskTid: ZonedDateTime,
                behandlingStatus: String,
                opprettetAv: String,
                saksnummer: String,
                saksbehandler: String): BehandlingDVH {
            return BehandlingDVH(
                    behandlingId = behandlingId,
                    aktorId = aktorId,
                    registrertTid = registrertTid,
                    endretTid = endretTid,
                    tekniskTid = tekniskTid,
                    sakYtelse = sakYtelse,
                    behandlingType = behandlingType,
                    behandlingStatus = behandlingStatus,
                    opprettetAv = opprettetAv,
                    opprettetEnhet = opprettetEnhet,
                    ansvarligEnhet = ansvarligEnhet,
                    behandlingMetode = behandlingMetode,
                    avsender = avsender,
                    saksnummer = saksnummer,
                    behandlingUuid = behandlingUuid,
                    mottattTid = mottattTid,
                    ferdigBehandletTid = ferdigBehandletTid,
                    vedtakTid = vedtakTid,
                    datoForsteUtbetaling = datoForsteUtbetaling,
                    sakUtland = sakUtland,
                    venteAarsak = venteAarsak,
                    behandlingResultat = behandlingResultat,
                    resultatBegrunnelse = resultatBegrunnelse,
                    saksbehandler = saksbehandler,
                    ansvarligBeslutter = ansvarligBeslutter,
                    versjon = versjon
            )
        }

    }
}