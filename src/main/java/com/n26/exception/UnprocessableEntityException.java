package com.n26.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason="Any of the fields are not parsable or the transaction date is in the future")
public class UnprocessableEntityException extends RuntimeException {}

