package no.nav.familie.ef.iverksett.infrastruktur.service

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.tilstand.IverksettResultatRepository
import no.nav.familie.ef.iverksett.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/jsonUpdate"])
@Unprotected
class JsonSchemaService(
    private val taskService: TaskService,
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettResultatRepository: IverksettResultatRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    @GetMapping
    fun update() {
        val iverksettingIder = iverksettingRepository.finnAlleIder()

        log.info("Starter oppretting av tasker for oppdatering av json på iverksett. Antall ${iverksettingIder.size}")
        iverksettingIder.forEach {
            taskService.save(Task(JsonUpdatePeriodeIverksettTask.TYPE, it.toString()))
        }
        log.info("oppretting av ${iverksettingIder.size} tasker fullført")

        val iverksettingresultater = iverksettResultatRepository.finnAlleIder()
        log.info(
            "Starter oppretting av tasker for oppdatering av json på iverksettResultat. " +
                "Antall ${iverksettingresultater.size}"
        )
        iverksettingresultater.forEach {
            taskService.save(Task(JsonUpdatePeriodeIverksettResultatTask.TYPE, it.toString()))
        }

        log.info("oppretting av ${iverksettingresultater.count()} tasker fullført")
    }
}

@Service
@TaskStepBeskrivelse(
    taskStepType = JsonUpdatePeriodeIverksettTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Oppdaterer Json-data."
)
class JsonUpdatePeriodeIverksettTask(
    private val iverksettingRepository: IverksettingRepository,
) : AsyncTaskStep {
    override fun doTask(task: Task) {

        val iverksett = iverksettingRepository.findByIdOrThrow(UUID.fromString(task.payload))
        when (iverksett.data) {
            is IverksettOvergangsstønad -> oppdater(iverksett, iverksett.data)
            is IverksettSkolepenger -> oppdater(iverksett, iverksett.data)
            is IverksettBarnetilsyn -> oppdater(iverksett, iverksett.data)
        }
    }

    private fun oppdater(it: Iverksett, data: IverksettOvergangsstønad) {
        val tilkjentYtelse = data.vedtak.tilkjentYtelse
        val oppdatertTilkjentYtelse = tilkjentYtelse?.copy(
            startdato = null,
            andelerTilkjentYtelse = tilkjentYtelse.andelerTilkjentYtelse.map { it.copy(fraOgMed = null, tilOgMed = null) }
        )
        val oppdatertVedtak = data.vedtak.copy(tilkjentYtelse = oppdatertTilkjentYtelse)
        val oppdaterteData = data.copy(vedtak = oppdatertVedtak)
        iverksettingRepository.update(it.copy(data = oppdaterteData))
    }

    private fun oppdater(it: Iverksett, data: IverksettBarnetilsyn) {
        val tilkjentYtelse = data.vedtak.tilkjentYtelse
        val oppdatertTilkjentYtelse = tilkjentYtelse?.copy(
            startdato = null,
            andelerTilkjentYtelse = tilkjentYtelse.andelerTilkjentYtelse.map { it.copy(fraOgMed = null, tilOgMed = null) }
        )
        val oppdaterteKontantstøtter = data.vedtak.kontantstøtte.map { it.copy(fraOgMed = null, tilOgMed = null) }
        val oppdaterteTilleggsstønander = data.vedtak.tilleggsstønad.map { it.copy(fraOgMed = null, tilOgMed = null) }

        val oppdatertVedtak = data.vedtak.copy(
            tilkjentYtelse = oppdatertTilkjentYtelse,
            kontantstøtte = oppdaterteKontantstøtter,
            tilleggsstønad = oppdaterteTilleggsstønander
        )
        val oppdaterteData = data.copy(vedtak = oppdatertVedtak)
        iverksettingRepository.update(it.copy(data = oppdaterteData))
    }

    private fun oppdater(it: Iverksett, data: IverksettSkolepenger) {
        val tilkjentYtelse = data.vedtak.tilkjentYtelse
        val oppdatertTilkjentYtelse = tilkjentYtelse?.copy(
            startdato = null,
            andelerTilkjentYtelse = tilkjentYtelse.andelerTilkjentYtelse.map { it.copy(fraOgMed = null, tilOgMed = null) }
        )
        val oppdaterteVedtaksperioder = data.vedtak.vedtaksperioder.map {
            val oppdatertePerioder = it.perioder.map { periode -> periode.copy(fraOgMed = null, tilOgMed = null) }
            it.copy(perioder = oppdatertePerioder)
        }

        val oppdatertVedtak =
            data.vedtak.copy(tilkjentYtelse = oppdatertTilkjentYtelse, vedtaksperioder = oppdaterteVedtaksperioder)
        val oppdaterteData = data.copy(vedtak = oppdatertVedtak)
        iverksettingRepository.update(it.copy(data = oppdaterteData))
    }

    companion object {
        const val TYPE = "JsonUpdatePeriodeIverksett"
    }
}

@Service
@TaskStepBeskrivelse(
    taskStepType = JsonUpdatePeriodeIverksettResultatTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Oppdaterer Json-data."
)
class JsonUpdatePeriodeIverksettResultatTask(private val iverksettResultatRepository: IverksettResultatRepository) :
    AsyncTaskStep {
    override fun doTask(task: Task) {
        val it = iverksettResultatRepository.findByIdOrThrow(UUID.fromString(task.payload))
        oppdater(it)
    }

    private fun oppdater(it: IverksettResultat) {
        val tilkjentYtelse = it.tilkjentYtelseForUtbetaling
        val oppdatertTilkjentYtelse = tilkjentYtelse?.copy(
            startdato = null,
            andelerTilkjentYtelse = tilkjentYtelse.andelerTilkjentYtelse.map { it.copy(fraOgMed = null, tilOgMed = null) }
        )
        iverksettResultatRepository.update(it.copy(tilkjentYtelseForUtbetaling = oppdatertTilkjentYtelse))
    }

    companion object {
        const val TYPE = "JsonUpdatePeriodeIverksettResultat"
    }
}
