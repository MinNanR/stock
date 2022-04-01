package site.minnan.stock.domain.vo;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import site.minnan.stock.domain.entity.MarketStatistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 涨跌停数据统计
 *
 * @author Minnan on 2022/04/01
 */
@Data
public class MarketLimitLineVO {

    /**
     * 涨停数据线
     */
    private List<Integer> surgedLineData;

    /**
     * 跌停数据线
     */
    private List<Integer> declineLineData;

    /**
     * 差值数据线
     */
    private List<Integer> differ;

    /**
     * 大盘指数线
     */
    private List<String> marketLineData;

    /**
     * 日期
     */
    private List<String> noteDate;

    public MarketLimitLineVO(){
        surgedLineData = new ArrayList<>();
        declineLineData = new ArrayList<>();
        differ = new ArrayList<>();
        marketLineData = new ArrayList<>();
        noteDate = new ArrayList<>();
    }

    public void add(MarketStatistics statistics){
        surgedLineData.add(statistics.getSurgedLimitCount());
        declineLineData.add(statistics.getDeclineLimitCount());
        differ.add(statistics.getSurgedLimitCount() - statistics.getDeclineLimitCount());
        noteDate.add(DateUtil.format(statistics.getNoteDate(), "yyyy-MM-dd"));
    }

    public void add(MarketStatistics statistics, BigDecimal market){
        surgedLineData.add(statistics.getSurgedLimitCount());
        declineLineData.add(statistics.getDeclineLimitCount());
        differ.add(statistics.getSurgedLimitCount() - statistics.getDeclineLimitCount());
        marketLineData.add(market.toString());
        noteDate.add(DateUtil.format(statistics.getNoteDate(), "yyyy-MM-dd"));
    }
}
