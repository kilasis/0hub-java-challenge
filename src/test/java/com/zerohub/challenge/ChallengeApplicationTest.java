package com.zerohub.challenge;

import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.PublishRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@Slf4j
public class ChallengeApplicationTest {
  private static final String BTC = "BTC";
  private static final String EUR = "EUR";
  private static final String USD = "USD";
  private static final String UAH = "UAH";
  private static final String RUB = "RUB";
  private static final String LTC = "LTC";

  @BeforeEach
  public void setup() {
    var rates = List.of(
      toPublishRequest(new String[]{BTC, EUR, "50000.0000"}),
      toPublishRequest(new String[]{EUR, USD, "1.2000"}),
      toPublishRequest(new String[]{RUB, USD, "80.0000"}),
      toPublishRequest(new String[]{UAH, RUB, "4.0000"}),
      toPublishRequest(new String[]{LTC, BTC, "0.0400"}),
      toPublishRequest(new String[]{LTC, USD, "2320.0000"})
    );
    for (var rate : rates) {
      // publish new data to service
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testData")
  void ConvertTest(String ignore, ConvertRequest request, BigDecimal expectedPrice) {

    // Request service and get price

    assertEquals(expectedPrice, new BigDecimal("0.0000"));
  }

  private static Stream<Arguments> testData() {

    return Stream.of(
      Arguments.of("Same currency", toConvertRequest(new String[]{BTC, BTC, "0.9997"}), "1.0000"),
      Arguments.of("Simple conversion", toConvertRequest(new String[]{EUR, BTC, "50000.0000"}), "1.0000"),
      Arguments.of("Reversed conversion", toConvertRequest(new String[]{BTC, EUR, "1.0000"}), "50000.0000"),
      Arguments.of("Convert with one hop", toConvertRequest(new String[]{BTC, USD, "1.0000"}), "60000.0000"),
      Arguments.of("Convert with two hops", toConvertRequest(new String[]{BTC, RUB, "1.0000"}), "4800000.0000"),
      Arguments.of("Reversed conversion with two hops", toConvertRequest(new String[]{RUB, EUR, "4800000.0000"}), "1.0000")
    );
  }

  private static PublishRequest toPublishRequest(String[] args) {
    return PublishRequest
      .newBuilder()
      .setBaseCurrency(args[0])
      .setQuoteCurrency(args[1])
      .setPrice(args[2])
      .build();
  }

  private static ConvertRequest toConvertRequest(String[] args) {
    return ConvertRequest
      .newBuilder()
      .setFromCurrency(args[0])
      .setToCurrency(args[1])
      .setFromAmount(args[2])
      .build();
  }
}
