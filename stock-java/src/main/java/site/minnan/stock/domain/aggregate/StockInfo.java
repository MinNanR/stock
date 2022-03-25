package site.minnan.stock.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 股票信息
 *
 * @author Minnan on 2022/03/24
 */
@TableName("stock_info")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockInfo {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 股票代码（带板块）
     */
    private String stockNickCode;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 是否需要统计
     */
    private Integer detected;
}
