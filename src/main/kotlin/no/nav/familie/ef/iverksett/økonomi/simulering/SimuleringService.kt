package no.nav.familie.ef.iverksett.økonomi.simulering

import no.nav.familie.ef.iverksett.featuretoggle.FeatureToggleService
import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.iverksetting.domene.Simulering
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.familie.ef.iverksett.økonomi.OppdragClient
import no.nav.familie.ef.iverksett.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.familie.felles.utbetalingsgenerator.domain.Utbetalingsoppdrag
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@Service
class SimuleringService(
    private val oppdragKlient: OppdragClient,
    private val iverksettResultatService: IverksettResultatService,
    private val featureToggleService: FeatureToggleService,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    private fun hentDetaljertSimuleringResultat(
        simulering: Simulering,
        brukNyUtbetalingsgenerator: Boolean,
    ): DetaljertSimuleringResultat {
        if (featureToggleService.isEnabled("familie.ef.iverksett.stopp-iverksetting")) {
            error("Kan ikke sende inn simmulere")
        }
        try {
            val forrigeTilkjentYtelse = simulering.forrigeBehandlingId?.let {
                iverksettResultatService.hentTilkjentYtelse(simulering.forrigeBehandlingId)
            }

            val brukNyUtbetalingsgeneratorFeature =
                featureToggleService.isEnabledMedFagsakId(
                    "familie.ef.iverksett.ny-utbetalingsgenerator",
                    simulering.nyTilkjentYtelseMedMetaData.eksternFagsakId,
                )

            val tilkjentYtelseMedUtbetalingsoppdrag = if (brukNyUtbetalingsgenerator || brukNyUtbetalingsgeneratorFeature) {
                log.info("Simulerer med ny utbetalingsgenerator for behandling=${simulering.nyTilkjentYtelseMedMetaData.behandlingId}")
                UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdragNy(
                    simulering.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse,
                )
            } else {
                log.info("Simulerer med gammel utbetalingsgenerator for behandling=${simulering.nyTilkjentYtelseMedMetaData.behandlingId}")
                UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simulering.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse,
                )
            }

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            if (utbetalingsoppdrag.utbetalingsperiode.isEmpty()) {
                return DetaljertSimuleringResultat(emptyList())
            }
            return hentSimuleringsresultatOgFiltrerPosteringer(
                utbetalingsoppdrag,
                simulering.nyTilkjentYtelseMedMetaData.stønadstype,
            )
        } catch (feil: Throwable) {
            val cause = feil.cause
            if (feil is RessursException && cause is HttpClientErrorException.BadRequest) {
                throw ApiFeil(feil.ressurs.melding, HttpStatus.BAD_REQUEST)
            }
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }

    fun hentBeriketSimulering(
        simulering: Simulering,
        brukNyUtbetalingsgenerator: Boolean = false,
    ): BeriketSimuleringsresultat {
        if (featureToggleService.isEnabled("familie.ef.iverksett.stopp-iverksetting")) {
            error("Kan ikke sende inn simmulere")
        }
        val detaljertSimuleringResultat = hentDetaljertSimuleringResultat(simulering, brukNyUtbetalingsgenerator)
        val simuleringsresultatDto = lagSimuleringsoppsummering(detaljertSimuleringResultat, LocalDate.now())

        return BeriketSimuleringsresultat(
            detaljer = detaljertSimuleringResultat,
            oppsummering = simuleringsresultatDto,
        )
    }

    private fun hentSimuleringsresultatOgFiltrerPosteringer(
        utbetalingsoppdrag: Utbetalingsoppdrag,
        stønadType: StønadType,
    ): DetaljertSimuleringResultat {
        val fagOmrådeKoder = fagområdeKoderForPosteringer(stønadType)
        val simuleringsResultat = oppdragKlient.hentSimuleringsresultat(utbetalingsoppdrag)
        return simuleringsResultat.copy(
            simuleringsResultat.simuleringMottaker
                .map { mottaker ->
                    mottaker.copy(
                        simulertPostering = mottaker.simulertPostering.filter { postering ->
                            fagOmrådeKoder.contains(postering.fagOmrådeKode)
                        },
                    )
                },
        )
    }
}
