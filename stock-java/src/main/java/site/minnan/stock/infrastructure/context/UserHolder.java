package site.minnan.stock.infrastructure.context;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import site.minnan.stock.application.provider.CommonUserService;
import site.minnan.stock.domain.entity.Principal;
import site.minnan.stock.infrastructure.utils.JwtUtil;
import site.minnan.stock.userinterface.response.ResponseCode;
import site.minnan.stock.userinterface.response.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

@Aspect
@Component
@ConditionalOnProperty(value = "jwt.authorized")
@Slf4j
public class UserHolder {

    @Value("${jwt.header}")
    private String AUTH_HEADER;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CommonUserService commonUserService;

    private static final String USER_ATTRIBUTE_NAME = "user";

    @Pointcut("execution(public * site.minnan.stock.userinterface.fascade..*..*(..))")
    private void user() {
    }

    @Pointcut("execution(public * site.minnan.stock.userinterface.fascade.AuthController..*(..))")
    private void loginUrl(){}

    @Around("user() && !loginUrl()")
    public Object setUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String header = request.getHeader(AUTH_HEADER);
        if(StrUtil.isBlank(header) || header.length() < 7){
            return ResponseEntity.invalid("非法用户");
        }
        String token = header.substring(7);
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            Principal principal = commonUserService.loadPrincipalByUserName(username);
            if (principal == null) {
                return ResponseEntity.invalid("非法用户");
            }
            if (!jwtUtil.validateToken(token, principal)) {
                return ResponseEntity.invalid("非法用户");
            }
            RequestContextHolder.currentRequestAttributes().setAttribute(USER_ATTRIBUTE_NAME, principal,
                    RequestAttributes.SCOPE_REQUEST);
            return joinPoint.proceed();
        } catch (ExpiredJwtException e){
            log.info("token已过期: {}", token);
            return ResponseEntity.fail(ResponseCode.INVALID_USER, "用户信息已过期");
        }
    }

    public static Principal getPrincipal() {
        return (Principal) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ATTRIBUTE_NAME,
                RequestAttributes.SCOPE_REQUEST);
    }
}
