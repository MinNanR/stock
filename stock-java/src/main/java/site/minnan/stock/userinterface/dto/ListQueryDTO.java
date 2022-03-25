package site.minnan.stock.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * 列表查询参数基类
 *
 * @author Minnan on 2020/12/16
 */
@Data
public class ListQueryDTO {

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    private Integer pageIndex;

    /**
     * 每页显示数量
     */
    @NotNull(message = "显示数量不能为空")
    private Integer pageSize;

    public Integer getStart() {
        return (pageIndex - 1) * pageSize;
    }
}
