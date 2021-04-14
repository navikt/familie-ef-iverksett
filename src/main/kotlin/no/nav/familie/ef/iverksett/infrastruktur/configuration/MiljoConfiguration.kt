package no.nav.familie.ef.iverksett.infrastruktur.configuration

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

class MiljoConfiguration {

    companion object {

        private val env by lazy { init() }

        private val isLocal = env["NAIS_CLUSTER_NAME"] == null
        private val clusterName = env["NAIS_CLUSTER_NAME"]
        private val local : String = "local"

        fun profiles(): Array<String> {
            return arrayOf(environment())
        }

        private fun environment(): String {
            if (isLocal) {
                return local
            }
            return clusterName
        }

        fun init(): Dotenv {
            return dotenv {
                ignoreIfMissing = true
                systemProperties = true
            }
        }
    }
}