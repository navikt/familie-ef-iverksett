package no.nav.familie.ef.iverksett.infrastruktur.configuration

import no.nav.familie.ef.iverksett.util.ObjectMapperProvider
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class JacksonConfig : WebMvcConfigurer {
    override fun extendMessageConverters(
        converters: MutableList<org.springframework.http.converter.HttpMessageConverter<*>>,
    ) {
        val jackson2Converter = MappingJackson2HttpMessageConverter(ObjectMapperProvider.objectMapper)
        converters.removeIf { it is MappingJackson2HttpMessageConverter }
        converters.add(0, jackson2Converter)
    }
}
