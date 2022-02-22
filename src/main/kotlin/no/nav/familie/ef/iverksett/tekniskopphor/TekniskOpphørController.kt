package no.nav.familie.ef.iverksett.tekniskopphor

import no.nav.familie.ef.iverksett.iverksetting.domene.TekniskOpphør
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.familie.kontrakter.ef.iverksett.TekniskOpphørDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping(path = ["/api/tekniskopphor"])
class TekniskOpphørController(val tekniskOpphørService: TekniskOpphørService) {

    @PostMapping
    fun iverksettTekniskOpphor(@RequestBody tekniskOpphørDto: TekniskOpphørDto) {
        val tekniskOpphør = TekniskOpphør(forrigeBehandlingId = tekniskOpphørDto.forrigeBehandlingId,
                                          tilkjentYtelseMedMetaData = TilkjentYtelseMedMetaData(
                                                  tilkjentYtelse = TilkjentYtelse(
                                                          andelerTilkjentYtelse = emptyList(),
                                                          startdato = null),
                                                  saksbehandlerId = tekniskOpphørDto.saksbehandlerId,
                                                  eksternBehandlingId = tekniskOpphørDto.eksternBehandlingId,
                                                  stønadstype = tekniskOpphørDto.stønadstype,
                                                  eksternFagsakId = tekniskOpphørDto.eksternFagsakId,
                                                  personIdent = tekniskOpphørDto.personIdent,
                                                  behandlingId = tekniskOpphørDto.behandlingId,
                                                  vedtaksdato = tekniskOpphørDto.vedtaksdato))
        tekniskOpphørService.startIverksettingAvTekniskOpphor(tekniskOpphør)
    }
}