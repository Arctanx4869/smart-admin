package com.xinhua.base.module.support.securityprotect.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SecurityPasswordService {

    private static final Argon2PasswordEncoder ARGON2_PASSWORD_ENCODER =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public String randomPassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    public static String getEncryptPwd(String password) {
        return ARGON2_PASSWORD_ENCODER.encode(password);
    }

    public static Boolean matchesPwd(String password, String encodedPassword) {
        return ARGON2_PASSWORD_ENCODER.matches(password, encodedPassword);
    }
}
