package com.example.demo.database.models.utils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class RestResponse<T> {

    private T body;

    @Setter(AccessLevel.NONE)
    private HttpStatus http_status;

    public void setHttp_status(HttpStatus http_status) {
        this.http_status = http_status;
        this.http_status_code = http_status.value();
    }

    private Integer http_status_code;
    private String message;
}
