package site.minnan.stock.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 股票历史价格
 *
 * @author Minnan on 2022/03/25
 */
@TableName("stock_price_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceHistory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 股票id
     */
    private Integer stockId;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 开盘价
     */
    private BigDecimal startPrice;

    /**
     * 收盘价
     */
    private BigDecimal endPrice;

    /**
     * 最高价
     */
    private BigDecimal highestPrice;

    /**
     * 最低价
     */
    private BigDecimal lowestPrice;

    /**
     * 成交量
     */
    private Integer volume;

    /**
     * 昨日收盘价
     */
    private BigDecimal endPriceLast;

    /**
     * 涨跌幅
     */
    private BigDecimal priceDifferRate;

    /**
     * 120天均价
     */
    @TableField("avg_price_past_120_days")
    private BigDecimal avgPricePast120Days;

    /**
     * 昨日120天均价
     */
    @TableField("avg_price_past_120_days_last")
    private BigDecimal avgPricePast120DaysLast;

    /**
     * 日期
     */
    private Date noteDate;

    /**
     * 入库时间
     */
    private Timestamp createTime;
}
