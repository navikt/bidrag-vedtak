package no.nav.bidrag.vedtak.exception

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
@Component
class RestExceptionHandler() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RestExceptionHandler::class.java)
    }

    @ResponseBody
    @ExceptionHandler(Exception::class)
    protected fun handleOtherExceptions(e: Exception): ResponseEntity<*> {
        val feilmelding = "Det skjedde en feil: ${e.message}"
        LOGGER.error(feilmelding, e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, feilmelding)
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(
        value = [
            IllegalArgumentException::class, MethodArgumentTypeMismatchException::class, ConversionFailedException::class,
            HttpMessageNotReadableException::class,
        ],
    )
    fun handleInvalidValueExceptions(exception: Exception): ResponseEntity<*> {
        val cause = exception.cause
        val valideringsFeil = if (cause is MismatchedInputException) createMissingKotlinParameterViolation(cause) else null
        LOGGER.error("Forespørselen inneholder ugyldig verdi: ${valideringsFeil ?: "ukjent feil"}", exception)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .header(HttpHeaders.WARNING, "Forespørselen inneholder ugyldig verdi: ${valideringsFeil ?: exception.message}")
            .build<Any>()
    }

    private fun createMissingKotlinParameterViolation(ex: MismatchedInputException): String {
        val errorFieldRegex = Regex("\\.([^.]*)\\[\\\"(.*)\"\\]\$")
        val paths = ex.path.map { errorFieldRegex.find(it.description)!! }.map {
            val (objectName, field) = it.destructured
            "$objectName.$field"
        }
        return "${paths.joinToString("->")} kan ikke være null"
    }
}
