package no.nav.familie.ef.iverksett.featuretoggle

interface FeatureToggleService {

    fun isEnabled(toggleId: String): Boolean {
        return isEnabled(toggleId, false)
    }

    fun isDisabled(toggleId: String): Boolean {
        return !isEnabled(toggleId, false)
    }

    fun isEnabled(toggleId: String, defaultValue: Boolean): Boolean
    fun isDisabled(toggleId: String, defaultValue: Boolean): Boolean
}
