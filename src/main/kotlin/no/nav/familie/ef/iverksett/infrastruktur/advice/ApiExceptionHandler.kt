package no.nav.familie.ef.iverksett.infrastruktur.advice

import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@Suppress("unused")
@ControllerAdvice
class ApiExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    private fun rootCause(throwable: Throwable): String {
        return NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<String> {
        val responseStatus = throwable::class.annotations.find { it is ResponseStatus }?.let { it as ResponseStatus }
        if (responseStatus != null) {
            return håndtertResponseStatusFeil(throwable, responseStatus)
        }
        return uventetFeil(throwable)
    }

    private fun uventetFeil(throwable: Throwable): ResponseEntity<String> {
        secureLogger.error("En feil har oppstått", throwable)
        logger.error("En feil har oppstått - throwable=${rootCause(throwable)} ")
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Uventet feil")
    }

    private fun håndtertResponseStatusFeil(
        throwable: Throwable,
        responseStatus: ResponseStatus
    ): ResponseEntity<String> {
        val status = if (responseStatus.value != HttpStatus.INTERNAL_SERVER_ERROR) responseStatus.value else responseStatus.code
        val loggMelding = "En håndtert feil har oppstått" +
                " throwable=${rootCause(throwable)}" +
                " reason=${responseStatus.reason}" +
                " status=$status"

        secureLogger.error(loggMelding, throwable)
        return ResponseEntity.status(status).body("Håndtert feil")
    }

    @ExceptionHandler(ApiFeil::class)
    fun handleApiFeil(feil: ApiFeil): ResponseEntity<String> {
        return ResponseEntity.status(feil.httpStatus).body(feil.feil)
    }

}
