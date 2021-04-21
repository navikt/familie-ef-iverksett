package no.nav.familie.ef.iverksett

import com.google.common.io.Resources
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

class ResourceLoaderTestUtil {

    companion object {

        @Autowired
        private val resourceLoader: ResourceLoader? = null

        fun toString(resource: Resource) : String {
            return Resources.toString(resource.url, StandardCharsets.UTF_8)
        }
        fun getResourceFrom(filename: String): Resource {
            return try {
                ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource("classpath:/json/$filename")
            } catch (ex: IOException) {
                val errorMsg = "Kunne ikke laste resource"
                throw RuntimeException(errorMsg, ex.cause)
            }
        }
    }
}