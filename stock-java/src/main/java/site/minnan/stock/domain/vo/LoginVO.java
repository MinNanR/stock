package site.minnan.stock.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功返回参数
 * @author Minnan on 2021/12/31
 */
@Data
@AllArgsConstructor
public class LoginVO {

    /**
     * 登录token
     */
    private String token;
}
