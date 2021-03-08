package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.function.Executable

@DisplayName("PeriodeServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeServiceTest {

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var periodePersistenceService: PeriodePersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny periode`() {
    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest("Test", "3333")
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.opprettetAv).isEqualTo(nyPeriodeRequest.opprettetAv) },
      Executable { assertThat(nyPeriodeOpprettet.enhetsnummer).isEqualTo(nyPeriodeRequest.enhetsnummer) }

    )
  }

  @Test
  fun `skal finne data for en periode`() {
    // Finner data for én periode
//    val nyPeriodeRequest = NyPeriodeRequest("Test", "3333")
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(PeriodeDto(opprettetAv = "TEST", enhetsnummer = "3333"))

    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.opprettetAv).isEqualTo(nyPeriodeRequest.opprettetAv) },
      Executable { assertThat(nyPeriodeOpprettet.enhetsnummer).isEqualTo(nyPeriodeRequest.enhetsnummer) }

    )
  }

  }


