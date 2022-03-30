package site.minnan.stock.application.service;


import site.minnan.stock.domain.vo.LoginVO;
import site.minnan.stock.userinterface.dto.LoginDTO;

public interface AuthUserService {

    /**
     * 登录校验
     *
     * @param dto 登录参数
     */
    LoginVO login(LoginDTO dto);
}
