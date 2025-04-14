package no.nav.bidrag.vedtak.exception.custom

import no.nav.bidrag.transport.felles.commonObjectmapper
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.nio.charset.Charset

class ConflictException(message: String, body: Any) :
    HttpClientErrorException(
        HttpStatus.CONFLICT,
        message,
        commonObjectmapper.writeValueAsBytes(body),
        Charset.defaultCharset(),
    )
