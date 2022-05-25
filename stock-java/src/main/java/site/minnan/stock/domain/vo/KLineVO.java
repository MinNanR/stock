package site.minnan.stock.domain.vo;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import site.minnan.stock.domain.entity.StockPriceHistory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * K线图页面展示数据
 *
 * @author Minnan on 2022/03/29
 */
@Data
public class KLineVO {

    /**
     * K线图数据
     */
    List<String[]> candlestickChartList;

    /**
     * 均线图数据
     */
    List<String> avgLineData;

    /**
     * 日期
     */
    List<String> dates;

    public void add(StockPriceHistory history) {
        String startPrice = history.getStartPrice().toString();
        String endPrice = history.getEndPrice().toString();
        String lowestPrice = history.getLowestPrice().toString();
        String highestPrice = history.getHighestPrice().toString();
        candlestickChartList.add(new String[]{startPrice, endPrice, lowestPrice, highestPrice});
        String avgPricePast120Days =
                Optional.ofNullable(history.getAvgPricePast120Days()).map(BigDecimal::toString).orElse(null);
        avgLineData.add(avgPricePast120Days);
        dates.add(DateUtil.format(history.getNoteDate(), "yyyy-MM-dd"));
    }

    public KLineVO() {
        this.candlestickChartList = new ArrayList<>();
        this.avgLineData = new ArrayList<>();
        this.dates = new ArrayList<>();
    }

}
