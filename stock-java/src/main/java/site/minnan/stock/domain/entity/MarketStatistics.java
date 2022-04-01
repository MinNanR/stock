package site.minnan.stock.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 市场数据统计
 *
 * @author Minnan on 2022/04/01
 */
@TableName("market_statistics")
@Data
public class MarketStatistics {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 统计日期
     */
    private Date noteDate;

    /**
     * 涨停个数
     */
    private Integer surgedLimitCount;

    /**
     * 跌停个数
     */
    private Integer declineLimitCount;

    /**
     * 创建时间
     */
    private Timestamp createTime;

}
