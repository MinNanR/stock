package site.minnan.stock.application.service;

import cn.hutool.json.JSONArray;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;

import java.util.List;

/**
 * 股票服务
 *
 * @author Minnan on 2022/03/24
 */
public interface StockService {

    /**
     * 添加股票
     *
     * @param stock
     */
    void addStock(StockInfo stock);

    /**
     * 批量添加股票
     *
     * @param stockInfoList
     */
    void addStockBatch(List<StockInfo> stockInfoList);

    /**
     * 查询历史股票数据
     *
     * @param stockInfo 股票信息
     * @param startDate 开始日期
     * @return
     */
    List<StockPriceHistory> fetchStockHistory(StockInfo stockInfo, String startDate);

    /**
     * 初始化股票价格
     *
     * @param priceHistoryList
     */
    void initStockPrice(List<StockPriceHistory> priceHistoryList);
}
