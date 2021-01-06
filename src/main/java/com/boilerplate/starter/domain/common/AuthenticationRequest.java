package com.boilerplate.starter.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationRequest {

    @NotNull
    private String username;

    @NotNull
    private String password;
}
