package site.minnan.stock.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import site.minnan.stock.domain.entity.Principal;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * 用户对象
 *
 * @author Minnan on 2021/12/31
 */
@TableName("auth_user")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUser {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 密码
     */
    private String password;

    /**
     * 密码戳
     */
    private String passwordStamp;

    /**
     * 创建人id
     */
    private Integer createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新人id
     */
    private Integer updateUserId;

    /**
     * 更新人id
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    public Principal principal() {
        Principal principal = new Principal();
        principal.setUserId(id);
        principal.setUsername(username);
        principal.setRealName(realName);
        principal.setPasswordStamp(passwordStamp);
        return principal;
    }

    public void createUser(Principal principal) {
        Timestamp now = Timestamp.from(Instant.now());
        this.createTime = now;
        this.updateTime = now;
        if (principal != null) {
            this.createUserId = principal.getUserId();
            this.createUserName = principal.getRealName();
            this.updateUserId = principal.getUserId();
            this.updateUserName = principal.getRealName();
        }
    }
}
