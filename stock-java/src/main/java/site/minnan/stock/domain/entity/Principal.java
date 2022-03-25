package site.minnan.stock.domain.entity;

import lombok.Data;

/**
 * 当前操作用户
 * @author Minnan on 2021/12/31
 */
@Data
public class Principal {

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户密码戳
     */
    private String passwordStamp;
}
