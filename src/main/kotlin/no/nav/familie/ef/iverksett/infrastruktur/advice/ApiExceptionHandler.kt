package no.nav.familie.ef.iverksett.infrastruktur.advice

import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Suppress("unused")
@ControllerAdvice
class ApiExceptionHandler : DefaultHandlerExceptionResolver() {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    private fun rootCause(throwable: Throwable): String {
        return NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName
    }

    @ExceptionHandler(Exception::class)
    fun handleThrowable(
        exception: Exception,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<String> {

        if (doResolveException(httpServletRequest, httpServletResponse, null, exception) != null) {
            return uventetFeil(
                exception,
                resolveStatus(httpServletResponse.status)
            )
        }
        return uventetFeil(exception, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ApiFeil::class)
    fun handleApiFeil(feil: ApiFeil): ResponseEntity<String> {
        return ResponseEntity.status(feil.httpStatus).body(feil.feil)
    }

    private fun resolveStatus(status: Int): HttpStatus {
        return HttpStatus.resolve(status) ?: HttpStatus.INTERNAL_SERVER_ERROR
    }

    private fun uventetFeil(throwable: Throwable, httpStatus: HttpStatus): ResponseEntity<String> {
        secureLogger.error("En feil har oppstått", throwable)
        logger.error("En feil har oppstått - throwable=${rootCause(throwable)}, status : ${httpStatus.value()} ")
        return ResponseEntity
            .status(httpStatus)
            .body("Uventet feil")
    }

}
