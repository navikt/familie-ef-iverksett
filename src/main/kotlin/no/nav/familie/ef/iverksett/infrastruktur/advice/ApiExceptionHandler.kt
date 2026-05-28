package no.nav.familie.ef.iverksett.infrastruktur.advice

import no.nav.familie.kontrakter.felles.Ressurs
import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Suppress("unused")
@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    private fun rootCause(throwable: Throwable): String = NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName

    override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        secureLogger.error("En feil har oppstått", ex)
        logger.error("En feil har oppstått - throwable=${rootCause(ex)} status=${status.value()}")
        return super.handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<Ressurs<Nothing>> {
        val responseStatus = throwable::class.annotations.find { it is ResponseStatus }?.let { it as ResponseStatus }
        if (responseStatus != null) {
            return håndtertResponseStatusFeil(throwable, responseStatus)
        }
        return uventetFeil(throwable)
    }

    private fun håndtertResponseStatusFeil(
        throwable: Throwable,
        responseStatus: ResponseStatus,
    ): ResponseEntity<Ressurs<Nothing>> {
        val status = if (responseStatus.value != HttpStatus.INTERNAL_SERVER_ERROR) responseStatus.value else responseStatus.code

        return ResponseEntity.status(status).body(Ressurs.failure("Håndtert feil"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<Ressurs<Nothing>> {
        logger.warn("Tilgang nektet - ${e.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Ressurs.ikkeTilgang(e.message ?: "Ingen tilgang"))
    }

    @ExceptionHandler(ApiFeil::class)
    fun handleApiFeil(feil: ApiFeil): ResponseEntity<Ressurs<Nothing>> = ResponseEntity.status(feil.httpStatus).body(Ressurs.failure(feil.feil))

    private fun resolveStatus(status: Int): HttpStatus = HttpStatus.resolve(status) ?: HttpStatus.INTERNAL_SERVER_ERROR

    private fun uventetFeil(throwable: Throwable): ResponseEntity<Ressurs<Nothing>> {
        secureLogger.error("En feil har oppstått", throwable)
        logger.error("En feil har oppstått - throwable=${rootCause(throwable)} ")
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Ressurs.failure("Uventet feil"))
    }
}
