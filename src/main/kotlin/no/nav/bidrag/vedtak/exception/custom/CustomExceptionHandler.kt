package no.nav.bidrag.vedtak.exception.custom

import no.nav.bidrag.commons.ExceptionLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Component
class CustomExceptionHandler(private val exceptionLogger: ExceptionLogger) {

    @ResponseBody
    @ExceptionHandler(VedtaksdataMatcherIkkeException::class)
    protected fun handleVedtaksdataMatcherIkkeException(e: VedtaksdataMatcherIkkeException): ResponseEntity<*> {
        exceptionLogger.logException(e, "CustomExceptionHandler")
        return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(PreconditionFailedException::class)
    fun handlePreconditionFailedException(e: PreconditionFailedException): ResponseEntity<String> {
        exceptionLogger.logException(e, "Precondition feilet")
        return ResponseEntity(e.message, HttpStatus.PRECONDITION_FAILED)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(e: ConflictException): ResponseEntity<String> {
        exceptionLogger.logException(e, "Innsendt unik-referanse finnes fra før")
        return ResponseEntity(e.message, HttpStatus.CONFLICT)
    }
}
