package no.nav.familie.ef.iverksett.infrastruktur.configuration

import no.nav.familie.ef.iverksett.util.JsonMapperProvider
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class JacksonConfig : WebMvcConfigurer {
    override fun extendMessageConverters(
        converters: MutableList<org.springframework.http.converter.HttpMessageConverter<*>>,
    ) {
        val jacksonJsonConverter = JacksonJsonHttpMessageConverter(JsonMapperProvider.jsonMapper)
        converters.removeIf { it is MappingJackson2HttpMessageConverter || it is JacksonJsonHttpMessageConverter }
        converters.add(0, jacksonJsonConverter)
    }
}
