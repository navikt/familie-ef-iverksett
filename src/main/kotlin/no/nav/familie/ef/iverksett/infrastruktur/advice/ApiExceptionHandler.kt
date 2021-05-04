package no.nav.familie.ef.iverksett.infrastruktur.advice

import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


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
        secureLogger.error("En feil har oppstått", throwable)
        logger.error("En feil har oppstått: ${rootCause(throwable)} ")

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("En uventet feil oppstod.")
    }

    @ExceptionHandler(ApiFeil::class)
    fun handleThrowable(feil: ApiFeil): ResponseEntity<String> {
        return ResponseEntity.status(feil.httpStatus).body(feil.feil)
    }

}
