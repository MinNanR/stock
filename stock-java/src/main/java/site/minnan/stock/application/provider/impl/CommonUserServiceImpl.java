package site.minnan.stock.application.provider.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.minnan.stock.application.provider.CommonUserService;
import site.minnan.stock.domain.aggregate.AuthUser;
import site.minnan.stock.domain.entity.Principal;
import site.minnan.stock.domain.mapper.AuthUserMapper;

import java.util.Optional;

@Service
public class CommonUserServiceImpl implements CommonUserService {

    @Autowired
    private AuthUserMapper authUserMapper;

    /**
     * 根据用户名加载用户信息
     *
     * @param username
     * @return
     */
    @Override
    @Cacheable(cacheNames = "user", key = "#username")
    public AuthUser loadUserByUserName(String username) {
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.last(" limit 1");
        return authUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Principal loadPrincipalByUserName(String username){
        return Optional.ofNullable(loadUserByUserName(username))
                .map(AuthUser::principal).orElse(null);
    }
}
