package no.nav.familie.ef.iverksett.featuretoggle

import no.finn.unleash.UnleashContext
import no.finn.unleash.strategy.Strategy

class ByEnvironmentStrategy : Strategy {

    companion object {

        private const val miljøKey = "miljø"
    }

    override fun getName(): String {
        return "byEnvironment"
    }

    override fun isEnabled(map: Map<String, String>): Boolean {
        return isEnabled(map, UnleashContext.builder().build())
    }

    override fun isEnabled(map: Map<String, String>, unleashContext: UnleashContext): Boolean {

        return unleashContext.environment
            .map { env -> map[miljøKey]?.split(',')?.contains(env) ?: false }
            .orElse(false)
    }
}
