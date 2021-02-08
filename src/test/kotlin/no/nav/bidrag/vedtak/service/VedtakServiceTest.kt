package no.nav.bidrag.vedtak.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("VedtakServiceTest")
class VedtakServiceTest {

  private val vedtakService: VedtakService = VedtakService()

  @Test
  @DisplayName("Skal opprette nytt vedtak")
  fun skalOppretteNyttVedtak() {
    vedtakService.nyttVedtak()
  }

  @Test
  @DisplayName("Skal finne data for vedtak")
  fun skalFinneDataForVedtak() {
    val vedtaksdata = vedtakService.finnVedtak("1")
    assertThat(vedtaksdata).isEqualTo("1")
  }
}
