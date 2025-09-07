package com.linkle.util;

import java.security.SecureRandom;

public class CodeGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateCode() {
        int number = random.nextInt(999999); // 0 ~ 999999
        return String.format("%06d", number); // 항상 6자리 (앞에 0 채움)
    }
}