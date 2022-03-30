package site.minnan.stock.application.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.Digester;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.minnan.stock.application.provider.CommonUserService;
import site.minnan.stock.application.service.AuthUserService;
import site.minnan.stock.domain.aggregate.AuthUser;
import site.minnan.stock.domain.entity.Principal;
import site.minnan.stock.domain.mapper.AuthUserMapper;
import site.minnan.stock.domain.vo.LoginVO;
import site.minnan.stock.infrastructure.exception.LoginException;
import site.minnan.stock.infrastructure.utils.JwtUtil;
import site.minnan.stock.userinterface.dto.LoginDTO;

/**
 * @author Minnan on 2021/12/31
 */
@Service
public class AuthUserServiceImpl implements AuthUserService {

    @Autowired
    private CommonUserService commonUserService;

    @Autowired
    private AuthUserMapper authUserMapper;

    @Autowired
    private Digester passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 登录校验
     *
     * @param dto
     * @return
     */
    @Override
    public LoginVO login(LoginDTO dto) {
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        AuthUser authUser = commonUserService.loadUserByUserName(dto.getUsername());
        if (authUser == null) {
            throw new LoginException("用户不存在");
        }
        String encodedPassword = passwordEncoder.digestHex(dto.getPassword());
        if (!StrUtil.equals(encodedPassword, authUser.getPassword())) {
            throw new LoginException("密码错误");
        }
        Principal principal = authUser.principal();
        String token = jwtUtil.generateToken(principal);
        return new LoginVO(token);
    }

}
