package no.nav.familie.ef.iverksett.infrastruktur.advice

import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

@RestControllerAdvice
class IverksettRequestBodyAdvice : RequestBodyAdviceAdapter() {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    override fun supports(
        methodParameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = targetType.typeName == IverksettDto::class.java.name

    override fun beforeBodyRead(
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>,
    ): HttpInputMessage {
        val body = inputMessage.body.readAllBytes()
        val jsonString = String(body, StandardCharsets.UTF_8)
        secureLogger.info("Mottatt IverksettDto JSON før deserialisering: {}", jsonString)
        return CachedHttpInputMessage(inputMessage, body)
    }

    private class CachedHttpInputMessage(
        private val original: HttpInputMessage,
        private val body: ByteArray,
    ) : HttpInputMessage {
        override fun getBody(): InputStream = ByteArrayInputStream(body)

        override fun getHeaders() = original.headers
    }
}
