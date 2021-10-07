package no.nav.familie.ef.iverksett.arena


import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RestController
@RequestMapping(path = ["/api/iverksett"])
@ProtectedWithClaims(issuer = "azuread")
@Profile("dev", "local")
class VedtakhendelseTestController(
        private val vedtakhendelseProducer: VedtakhendelseProducer
) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @PostMapping("/vedtakhendelse", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendVedtakhendelse(@RequestBody aktørId: String) {
        vedtakhendelseProducer.produce(VedtakHendelser(
            aktoerID = aktørId,
            avslutningsstatus = "innvilget",
            behandlingstema = Behandlingstema.valueOf(StønadType.OVERGANGSSTØNAD.name.lowercase(Locale.getDefault())
                                                          .replaceFirstChar { it.uppercase() }).value,
            hendelsesprodusentREF = "EF",
            applikasjonSakREF = aktørId,
            hendelsesTidspunkt = LocalDateTime.now().format(dateTimeFormatter)
        ))
    }

}