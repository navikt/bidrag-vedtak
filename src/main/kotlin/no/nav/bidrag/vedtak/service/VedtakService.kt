package no.nav.bidrag.vedtak.service

import org.springframework.stereotype.Service

@Service
class VedtakService {

  fun finnVedtak(vedtaksnummer: String): String {
    return vedtaksnummer
  }

  //  public Optional<VedtakDto> finnVedtak(String vedtaksnummer) {
  //    var vedtak = Optional.ofNullable(vedtakReposistory.findByVedtaksnummer(vedtaksnummer));
  //    return vedtak.map(Vedtak::tilVedtakDto);
  //  }
  fun nyttVedtak() {} //  public NyttVedtakResponseDto nyttVedtak(NyttVedtakCommandDto nyttVedtakCommandDto) {
  //    var nyttVedtaksnummernummer = hentNyttVedtaksnummerFraDatabase();
  //    nyttVedtakReposistory.save(
  //        new NyttVedtak(nyttVedtaksnummer, nyttVedtakCommandDto.getxxx()));
  //    tilgangRepository.save(new Tilgang(nyttVedtaksnummer, nyttVedtakCommandDto.getxxx()));
  //
  //    return new NyttVedtakResponseDto(nyttVedtaksnummer);
  //  }
  //  private String hentNyttVedtaksnummerFraDatabase() {
  //    Integer maxLopenummer =
  //        nyttVedtakReposistory.hentMaxLoepenummerSomIkkeOverskrider(
  //            VedtaksnummerSerie.hentMaksimumsgrenseForAarstall());
  //
  //    String nyttVedtaksnummer;
  //
  //    if (maxLopenummer == null || maxLopenummer < VedtaksnummerSerie.hentMinimumsgrenseForAarstall()) {
  //      nyttVedtaksnummer = String.valueOf(VedtaksnummerSerie.hentMinimumsgrenseForAarstall());
  //    } else {
  //      nyttVedtaksnummer = String.valueOf(maxLopenummer + 1);
  //    }
  //
  //    return nyttVedtaksnummer;
  //  }
}