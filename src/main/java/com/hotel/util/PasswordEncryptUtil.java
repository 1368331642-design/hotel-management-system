package com.hotel.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类（独立使用，无需依赖Spring Security上下文）
 * 功能：1. 加密明文密码 2. 校验明文密码和加密密码是否匹配
 */
public class PasswordEncryptUtil {

    // 单例BCrypt编码器（避免重复创建，提升性能）
    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密明文密码（不可逆，每次加密结果不同但校验一致）
     * @param rawPassword 明文密码（如：123456）
     * @return 加密后的密码（如：$2a$10$xxxxxxx...）
     */
    public static String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码：明文密码 vs 加密密码
     * @param rawPassword 前端传入的明文密码
     * @param encryptedPassword 数据库存储的加密密码
     * @return true-匹配，false-不匹配
     */
    public static boolean verifyPassword(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return BCRYPT_ENCODER.matches(rawPassword, encryptedPassword);
    }

//    // 测试方法（可选：运行main方法生成加密密码，直接替换数据库中的明文）
//    public static void main(String[] args) {
//        // 示例：生成"123456"的加密密码，复制到数据库替换明文
//        String rawPwd = "123456";
//        String encryptedPwd = encryptPassword(rawPwd);
//        System.out.println("明文密码：" + rawPwd);
//        System.out.println("加密密码：" + encryptedPwd);
//        // 校验示例
//        boolean match = verifyPassword(rawPwd, encryptedPwd);
//        System.out.println("密码校验结果：" + match); // 输出true
//    }
}
