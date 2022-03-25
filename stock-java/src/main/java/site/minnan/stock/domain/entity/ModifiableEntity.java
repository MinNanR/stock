package site.minnan.stock.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 可修改的实体类，用于记录创建时间与修改时间
 *
 * @author Minnan on 2021/3/15
 */
@NoArgsConstructor
@Getter
public abstract class ModifiableEntity {

    /**
     * 创建者id
     */
    private Integer createUserId;

    /**
     * 创建者姓名
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
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    /**
     * 设置创建者
     *
     * @param principal 创建者
     */
    public abstract void setCreateUser(Principal principal);

    /**
     * 设置更新者
     *
     * @param principal 更新者
     */
    public abstract void setUpdateUser(Principal principal);

    public void setCreateUser(Integer userId, String username, Timestamp createTime) {
        this.createUserId = this.updateUserId = userId;
        this.createUserName = this.updateUserName = username;
        this.createTime = this.updateTime = createTime;
    }

    public void setUpdateUser(Integer userId, String username, Timestamp createTime) {
        this.updateUserId = userId;
        this.updateUserName = username;
        this.updateTime = createTime;
    }

    protected void setCreateUser(Integer userId, String username) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        setCreateUser(userId, username, time);
    }

    protected void setUpdateUser(Integer userId, String username) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        setUpdateUser(userId, username, time);
    }
}
