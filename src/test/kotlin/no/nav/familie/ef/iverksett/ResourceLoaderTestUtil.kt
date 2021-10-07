package no.nav.familie.ef.iverksett

import java.nio.charset.StandardCharsets

object ResourceLoaderTestUtil {

    fun readResource(name: String): String {
        return this::class.java.classLoader.getResource(name)!!.readText(StandardCharsets.UTF_8)
    }
}