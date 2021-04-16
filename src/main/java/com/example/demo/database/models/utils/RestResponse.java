package com.example.demo.database.models.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class RestResponse<T> {

    private T body;
    private HttpStatus http_status;
    private String message;
}
