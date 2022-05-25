package site.minnan.stock.application.service;

import site.minnan.stock.domain.vo.MarketLimitLineVO;
import site.minnan.stock.infrastructure.exception.ProcessingException;

import java.util.Date;
import java.util.List;

/**
 * 统计类service
 *
 * @author Minnan on 2022/04/01
 */
public interface StatisticsService {

    /**
     * 市场统计任务
     *
     * @param date
     */
    void marketStatistics(String date);

    /**
     * 查询市场涨停跌停数量数据
     *
     * @return
     */
    MarketLimitLineVO getMarketLimitData() throws ProcessingException;

    /**
     * 获取过去120个交易日
     *
     * @return
     */
    List<Date> getTradeDateListPast120();
}
