package com.zerohub.challenge.service;

import com.google.protobuf.Empty;
import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Slf4j
@GrpcService
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RatesService extends RatesServiceGrpc.RatesServiceImplBase {
  private final CurrencyConverter currencyConverter;

  @Override
  public void publish(PublishRequest request, StreamObserver<Empty> responseObserver) {
    currencyConverter.addRate(request.getBaseCurrency(), request.getQuoteCurrency(), new BigDecimal(request.getPrice()));
    log.info("Currency rate was published successfully: '{}'", request);

    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void convert(ConvertRequest request, StreamObserver<ConvertResponse> responseObserver) {
    String fromAmount = request.getFromAmount();
    String fromCurrency = request.getFromCurrency();
    String toCurrency = request.getToCurrency();

    try {
      log.debug("Trying to convert from currency: '{}', to currency: '{}', from amount: '{}'", fromCurrency, toCurrency, fromAmount);
      BigDecimal convertedAmount = currencyConverter.convert(fromCurrency, toCurrency, new BigDecimal(fromAmount));
      log.info("Currency was converted successfully. From: '{} {}', To: '{} {}'", fromAmount, fromCurrency, convertedAmount, toCurrency);

      ConvertResponse convertResponse = ConvertResponse.newBuilder()
          .setPrice(convertedAmount.toString())
          .build();

      responseObserver.onNext(convertResponse);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Can't convert from currency: '{}', to currency: '{}', from amount: '{}'",
          fromCurrency, toCurrency, fromAmount, e);
      responseObserver.onError(e);
    }
  }
}
