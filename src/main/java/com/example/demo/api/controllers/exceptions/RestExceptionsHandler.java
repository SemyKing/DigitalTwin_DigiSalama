package com.example.demo.api.controllers.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class RestExceptionsHandler {

    @NonNull
    @JsonProperty
    private Integer status;

    @NonNull
    @JsonProperty
    private String message;
}
