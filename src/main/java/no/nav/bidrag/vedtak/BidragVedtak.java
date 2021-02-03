package no.nav.bidrag.vedtak;

import static no.nav.bidrag.vedtak.BidragVedtakConfig.LIVE_PROFILE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BidragVedtak {

  public static void main(String[] args) {

    String profile = args.length < 1 ? LIVE_PROFILE : args[0];

    SpringApplication app = new SpringApplication(BidragVedtak.class);
    app.setAdditionalProfiles(profile);
    app.run(args);
  }
}
