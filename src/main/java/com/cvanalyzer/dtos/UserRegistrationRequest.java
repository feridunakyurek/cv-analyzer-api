package com.cvanalyzer.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserRegistrationRequest {

    @NotBlank(message = "Email boş bırakılamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre boş bırakılamaz")
    private String password;

    private String confirmPassword;

    private String name;
    private String surname;
}