package no.nav.familie.ef.iverksett.infrastruktur.service

import no.nav.familie.ef.iverksett.iverksetting.IverksettingRepository
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/jsonUpdate"])
@Unprotected
class JsonSchemaService(private val iverksettingRepository: IverksettingRepository) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    @GetMapping
    fun update() {
        val iverksettinger = iverksettingRepository.findAll()

        log.info("Starter oppdatering av ${iverksettinger.count()}")
        iverksettinger.forEach {

            when (it.data) {
                is IverksettOvergangsstønad -> oppdater(it, it.data)
                is IverksettSkolepenger -> oppdater(it, it.data)
                is IverksettBarnetilsyn -> oppdater(it, it.data)
            }
        }
        log.info("Oppdatering av ${iverksettinger.count()} iverksettinger fullført")
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
}
