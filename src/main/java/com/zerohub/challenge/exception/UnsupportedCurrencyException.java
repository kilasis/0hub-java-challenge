package com.zerohub.challenge.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class UnsupportedCurrencyException extends StatusRuntimeException {
  public UnsupportedCurrencyException() {
    super(Status.NOT_FOUND);
  }
}
