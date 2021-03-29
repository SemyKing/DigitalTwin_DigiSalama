package com.example.demo.database.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor@NoArgsConstructor
public class PasswordUpdateResponse {

    private String password_update_token;
}
