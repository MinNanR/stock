package site.minnan.stock.infrastructure.utils;

import cn.hutool.core.date.DateTime;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.minnan.stock.domain.entity.Principal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * JWT相关操作
 * @author Minnan on 2021/12/31
 */
@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private long JWT_TOKEN_VALIDITY;

    @Value("${jwt.secret}")
    private String secret;

    //从token中解析用户名
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //从token中解析过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //解析token
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(expiration);
    }

    public String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String generateToken(Principal principal){
        Map<String, Object> claims = new HashMap<>();
        claims.put("realName", principal.getRealName());
        claims.put("id", principal.getUserId());
        claims.put("passwordStamp", principal.getPasswordStamp());
        return doGenerateToken(claims, principal.getUsername());
    }

    public Boolean validateToken(String token, Principal principal){
        Claims claims = getAllClaimsFromToken(token);
        String username = claims.getSubject();
        String passwordStamp = claims.get("passwordStamp", String.class);
        Date expiration = claims.getExpiration();
        return Objects.equals(username, principal.getUsername())
                && Objects.equals(passwordStamp, principal.getPasswordStamp())
                && DateTime.now().isBefore(expiration);
    }
}
