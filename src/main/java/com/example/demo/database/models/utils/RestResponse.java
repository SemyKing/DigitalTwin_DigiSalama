package com.example.demo.database.models.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class RestResponse<T> {

    T body;
    private HttpStatus http_status;
    private String message;
}
