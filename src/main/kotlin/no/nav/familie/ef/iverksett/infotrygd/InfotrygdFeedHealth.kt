package no.nav.familie.ef.iverksett.infotrygd

import no.nav.familie.http.health.AbstractHealthIndicator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!local")
class InfotrygdFeedHealth(infotrygdFeedClient: InfotrygdFeedClient) :
    AbstractHealthIndicator(infotrygdFeedClient, "infotrygd.feed")
