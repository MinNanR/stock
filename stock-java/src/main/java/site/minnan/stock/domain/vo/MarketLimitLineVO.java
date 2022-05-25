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
     * 上证指数指数线
     */
    private List<String> sh000001;

    /**
     * 上证50指数线
     */
    private List<String> sh000016;

    /**
     * 中证500指数线
     */
    private List<String> sh000905;

    /**
     * 沪深300指数线
     */
    private List<String> sz399300;

    /**
     * 日期
     */
    private List<String> noteDate;

    public MarketLimitLineVO() {
        surgedLineData = new ArrayList<>();
        declineLineData = new ArrayList<>();
        differ = new ArrayList<>();
        sh000001 = new ArrayList<>();
        sh000016 = new ArrayList<>();
        sh000905 = new ArrayList<>();
        sz399300 = new ArrayList<>();
        noteDate = new ArrayList<>();
    }

    public void add(MarketStatistics statistics) {
        surgedLineData.add(statistics.getSurgedLimitCount());
        declineLineData.add(statistics.getDeclineLimitCount());
        differ.add(statistics.getSurgedLimitCount() - statistics.getDeclineLimitCount());
        noteDate.add(DateUtil.format(statistics.getNoteDate(), "yyyy-MM-dd"));
    }

    public void add(MarketStatistics statistics, BigDecimal market) {
        surgedLineData.add(statistics.getSurgedLimitCount());
        declineLineData.add(statistics.getDeclineLimitCount());
        differ.add(statistics.getSurgedLimitCount() - statistics.getDeclineLimitCount());
        this.sh000001.add(market.toString());
        noteDate.add(DateUtil.format(statistics.getNoteDate(), "yyyy-MM-dd"));
    }

    public void add(MarketStatistics statistics, BigDecimal sh000001, BigDecimal sh000016, BigDecimal sh000905,
                    BigDecimal sz399300) {
        surgedLineData.add(statistics.getSurgedLimitCount());
        declineLineData.add(statistics.getDeclineLimitCount());
        differ.add(statistics.getSurgedLimitCount() - statistics.getDeclineLimitCount());

        this.sh000001.add(sh000001.toString());
        this.sh000016.add(sh000016.toString());
        this.sh000905.add(sh000905 == null ? null : sh000905.toString());
        this.sz399300.add(sz399300 == null ? null : sz399300.toString());

        noteDate.add(DateUtil.format(statistics.getNoteDate(), "yyyy-MM-dd"));
    }
}
