package no.nav.familie.ef.iverksett.økonomi

import no.nav.familie.http.health.AbstractHealthIndicator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!local")
class OppdragHealth(client: OppdragClient) :
    AbstractHealthIndicator(client, "familie.oppdrag")
