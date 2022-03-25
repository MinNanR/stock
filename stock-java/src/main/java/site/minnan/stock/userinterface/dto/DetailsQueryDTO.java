package site.minnan.stock.userinterface.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * 查询详情通用参数
 * @author Minnan on 2020/12/21
 */
@Data
public class DetailsQueryDTO {

    @NotNull(message = "未指定查询对象")
    private Integer id;
}
