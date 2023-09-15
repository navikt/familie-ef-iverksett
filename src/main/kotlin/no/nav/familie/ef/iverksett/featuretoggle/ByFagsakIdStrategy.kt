package no.nav.familie.ef.iverksett.featuretoggle

import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy
import org.slf4j.MDC

class ByFagsakIdStrategy : Strategy {

    override fun getName(): String {
        return "byFagsakId"
    }

    override fun isEnabled(map: Map<String, String>): Boolean {
        return isEnabled(map, UnleashContext.builder().build())
    }

    override fun isEnabled(map: Map<String, String>, unleashContext: UnleashContext): Boolean {
        return erRiktigFagsak(map) &&
            erRiktigMiljø(unleashContext, map)
    }

    private fun erRiktigFagsak(map: Map<String, String>): Boolean {
        val fagsakId = MDC.get(MDC_FEATURE_FAGSAK_ID)
        return map["fagsakId"]
            ?.split(',')
            ?.any { fagsakId == it }
            ?: false
    }

    private fun erRiktigMiljø(
        unleashContext: UnleashContext,
        map: Map<String, String>,
    ): Boolean = unleashContext.environment
        .map { env -> map["miljø"]?.split(',')?.contains(env) ?: false }
        .orElse(false)
}
