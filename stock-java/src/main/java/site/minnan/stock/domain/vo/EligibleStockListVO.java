package site.minnan.stock.domain.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 符合条件股票查询返回数据
 *
 * @author Minnan on 2022/03/29
 */
@Data
public class EligibleStockListVO {

    /**
     * 记录id
     */
    private Integer id;

    /**
     * 股票id
     */
    private Integer stockId;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 开盘价
     */
    private String startPrice;

    /**
     * 收盘价
     */
    private String endPrice;

    /**
     * 最高价
     */
    private String highestPrice;

    /**
     * 最低价
     */
    private String lowestPrice;

    /**
     * 120天均价
     */
    private String avgPricePast120Days;
}
