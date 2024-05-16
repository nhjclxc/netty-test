package com.nhjclxc.nettytest.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LuoXianchao
 * @since 2023/10/03 16:58
 */
@Component
public class TokenUtils {

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;


    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_id";

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims) {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, "abcdefghijklmnopqrstuvwxyz").compact();
        return token;
    }


    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }


    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenUtils.TOKEN_PREFIX)) {
            token = token.replace(TokenUtils.TOKEN_PREFIX, "");
        }
        return token;
    }


    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put(LOGIN_USER_KEY, 678L);
        String token = createToken(map);
        System.out.println(token);

        //123L eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2lkIjoxMjN9.UUiE5ViVNQaAk0jRGNxSlzJg5zuoqCO4q2sI9ZqewUZvjFjO3FkvGB7WaXTejib2nyDG7v2x8XFH-cAgybdi8A
        //678L eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2lkIjo2Nzh9.qYW1dHxbNsX_KNgSedRvUTAo_116V7KB5UyFZ5007oNWQsEbckW5fj2mRhS20IvQbTHD9SVATKUntuUy39jSjg
    }
}
