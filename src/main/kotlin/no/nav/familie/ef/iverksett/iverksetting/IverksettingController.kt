package no.nav.familie.ef.iverksett.iverksetting

import no.nav.familie.ef.iverksett.infrastruktur.advice.ApiFeil
import no.nav.familie.ef.iverksett.infrastruktur.transformer.toDomain
import no.nav.familie.ef.iverksett.iverksetting.domene.Brev
import no.nav.familie.ef.iverksett.iverksetting.domene.Iverksett
import no.nav.familie.ef.iverksett.tilbakekreving.validerTilbakekreving
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettStatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping(
    path = ["/api/iverksett"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ProtectedWithClaims(issuer = "azuread")
class IverksettingController(
    private val iverksettingService: IverksettingService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun iverksett(
        @RequestPart("data") iverksettDto: IverksettDto,
        @RequestPart("fil") fil: MultipartFile
    ) {
        val iverksett = iverksettDto.toDomain()
        valider(iverksett)
        validerSkalHaBrev(iverksett)
        iverksettingService.startIverksetting(iverksett, opprettBrev(iverksettDto, fil))
    }

    @PostMapping("migrering", "uten-brev")
    fun iverksett(@RequestBody iverksettDto: IverksettDto) {
        val iverksett = iverksettDto.toDomain()
        valider(iverksett)
        validerUtenBrev(iverksett)
        iverksettingService.startIverksetting(iverksett, null)
    }

    @GetMapping("/status/{behandlingId}")
    fun hentStatus(@PathVariable behandlingId: UUID): ResponseEntity<IverksettStatus> {
        val status = iverksettingService.utledStatus(behandlingId)
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @PostMapping("/vedtakshendelse/{behandlingId}")
    fun publiserVedtakshendelser(@PathVariable behandlingId: UUID): ResponseEntity<Any> {
        iverksettingService.publiserVedtak(behandlingId)
        return ResponseEntity.ok().build()
    }

    private fun opprettBrev(iverksettDto: IverksettDto, fil: MultipartFile): Brev {
        return Brev(iverksettDto.behandling.behandlingId, fil.bytes)
    }

    private fun validerUtenBrev(iverksett: Iverksett) {
        if (!iverksett.skalIkkeSendeBrev()) {
            throw ApiFeil(
                "Kan ikke ha iverksetting uten brev n??r det ikke er en migrering, " +
                    "g-omregning eller korrigering uten brev ",
                HttpStatus.BAD_REQUEST
            )
        }
    }

    private fun validerSkalHaBrev(iverksett: Iverksett) {
        if (iverksett.skalIkkeSendeBrev()) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med brev n??r det er migrering, g-omregning eller korrigering uten brev",
                HttpStatus.BAD_REQUEST
            )
        }
    }

    private fun valider(iverksett: Iverksett) {
        if (iverksett.vedtak.tilkjentYtelse == null && iverksett.vedtak.vedtaksresultat != Vedtaksresultat.AVSL??TT) {
            throw ApiFeil(
                "Kan ikke ha iverksetting uten tilkjentYtelse " +
                    "for vedtak med resultat=${iverksett.vedtak.vedtaksresultat}",
                HttpStatus.BAD_REQUEST
            )
        }
        if (iverksett.vedtak.tilkjentYtelse != null && iverksett.vedtak.vedtaksresultat == Vedtaksresultat.AVSL??TT) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med tilkjentYtelse " +
                    "for vedtak med resultat=${iverksett.vedtak.vedtaksresultat}",
                HttpStatus.BAD_REQUEST
            )
        }

        if (!iverksett.vedtak.tilbakekreving.validerTilbakekreving())
            throw ApiFeil("Tilbakekreving er ikke gyldig", HttpStatus.BAD_REQUEST)
    }
}
