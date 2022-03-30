package site.minnan.stock.userinterface.dto;

import lombok.Data;

/**
 * 查询符合条件的股票参数
 */
@Data
public class GetEligibleStockListDTO extends ListQueryDTO{

    /**
     * 统计日期
     */
    private String noteDate;
}
