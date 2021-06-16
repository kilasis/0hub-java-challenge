package com.zerohub.challenge;

import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@Slf4j
@SpringBootTest(properties = {
    "grpc.server.inProcessName=test", // Enable inProcess server
    "grpc.server.port=-1", // Disable external server
    "grpc.client.inProcess.address=in-process:test" // Configure the client to connect to the inProcess server
})
class ChallengeApplicationTest {
  private static final String BTC = "BTC";
  private static final String EUR = "EUR";
  private static final String USD = "USD";
  private static final String UAH = "UAH";
  private static final String RUB = "RUB";
  private static final String LTC = "LTC";
  private static final String AUD = "AUD";
  private static final String KZT = "KZT";

  @GrpcClient("inProcess")
  private RatesServiceGrpc.RatesServiceBlockingStub sut;

  @BeforeEach
  public void setup() {
    var rates = List.of(
        toPublishRequest(new String[]{BTC, EUR, "50000.0000"}),
        toPublishRequest(new String[]{EUR, USD, "1.2000"}),
        toPublishRequest(new String[]{EUR, AUD, "1.5000"}),
        toPublishRequest(new String[]{AUD, KZT, "328.65"}),
        toPublishRequest(new String[]{USD, RUB, "80.0000"}),
        toPublishRequest(new String[]{UAH, RUB, "4.0000"}),
        toPublishRequest(new String[]{LTC, BTC, "0.0400"}),
        toPublishRequest(new String[]{LTC, USD, "2320.0000"})
    );

    rates.forEach(rate -> sut.publish(rate));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("convertTestData")
  void convertTest(String ignore, ConvertRequest request, BigDecimal expectedPrice) {

    ConvertResponse convert = sut.convert(request);

    assertEquals(expectedPrice, new BigDecimal(convert.getPrice()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("convertTestForUnsupportedCurrencyData")
  void convertTestForUnsupportedCurrency(String ignore, ConvertRequest request) {
    StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
        () -> sut.convert(request));

    assertEquals(Status.NOT_FOUND, exception.getStatus());
  }

  private static Stream<Arguments> convertTestData() {
    return Stream.of(
        Arguments.of("Same currency", toConvertRequest(new String[]{BTC, BTC, "0.9997"}), "0.9997"),
        Arguments.of("Simple conversion", toConvertRequest(new String[]{EUR, BTC, "50000.0000"}), "1.0000"),
        Arguments.of("Reversed conversion", toConvertRequest(new String[]{BTC, EUR, "1.0000"}), "50000.0000"),
        Arguments.of("Convert with one hop", toConvertRequest(new String[]{BTC, AUD, "1.0000"}), "75000.0000"),
        Arguments.of("Convert with two hops", toConvertRequest(new String[]{BTC, KZT, "1.0000"}), "24648750.0000"),
        Arguments.of("Reversed conversion with two hops", toConvertRequest(new String[]{RUB, EUR, "96.0000"}), "1.0000"),
        Arguments.of("Small value conversion", toConvertRequest(new String[]{USD, BTC, "4.0000"}), "0.0001")
    );
  }

  private static Stream<Arguments> convertTestForUnsupportedCurrencyData() {
    return Stream.of(
        Arguments.of("Wrong fromCurrency", toConvertRequest(new String[]{"UNSUPPORTED_CURRENCY", BTC, "0.9997"})),
        Arguments.of("Wrong toCurrency", toConvertRequest(new String[]{EUR, "UNSUPPORTED_CURRENCY", "50000.0000"}))
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
