package site.minnan.stock.infrastructure.config;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizationConfig {

    @Bean
    public Digester getDigester(){
        return DigestUtil.digester(DigestAlgorithm.SHA256);
    }
}
