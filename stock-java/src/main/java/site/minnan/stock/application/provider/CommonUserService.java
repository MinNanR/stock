package site.minnan.stock.application.provider;

import site.minnan.stock.domain.aggregate.AuthUser;
import site.minnan.stock.domain.entity.Principal;

/**
 * 用于记载用户信息的service
 * @author Minnan on 2021/01/02
 */
public interface CommonUserService {

    /**
     * 根据用户名加载用户信息
     *
     * @return
     */
    AuthUser loadUserByUserName(String username);

    Principal loadPrincipalByUserName(String username);
}
