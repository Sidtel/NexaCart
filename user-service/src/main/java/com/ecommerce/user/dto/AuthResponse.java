package com.ecommerce.user.dto;

import com.ecommerce.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private String userId;
    private String email;
    private String username;
    private Set<Role> roles;
    private long expiresIn;
}
