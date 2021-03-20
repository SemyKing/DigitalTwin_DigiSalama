package com.example.demo.api.handlers;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class RestResponseHandler<T> {

    T body;

    private HttpStatus httpStatus;

    private String message;
}
