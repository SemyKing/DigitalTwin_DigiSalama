package com.example.demo.database.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor@NoArgsConstructor
public class PasswordUpdateRequest {

    private String password_update_token = "";
    private String new_password = "";
}
