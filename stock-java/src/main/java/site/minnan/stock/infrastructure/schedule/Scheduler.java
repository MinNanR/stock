package site.minnan.stock.infrastructure.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.minnan.stock.application.service.StatisticsService;
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.infrastructure.utils.RedisUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class Scheduler {

    @Autowired
    private StockService stockService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RedisUtil redisUtil;

    private static final BigDecimal surgedUp = new BigDecimal("0.101");
    private static final BigDecimal surgedDown = new BigDecimal("0.098");
    private static final BigDecimal declineUp = new BigDecimal("-0.098");
    private static final BigDecimal declineDown = new BigDecimal("-0.101");

    @Scheduled(cron = "0 0 16 * * *")
    public void getTodayPrice() {
        DateTime now = DateTime.now();
        List<Date> tradeDateList = statisticsService.getTradeDateListPast120();
        if (CollectionUtil.isEmpty(tradeDateList)) {
            log.error("未查询到交易日期");
            return;
        }
        Date newestTradeDate = tradeDateList.get(0);
        String today = now.toString("yyyyMMdd");
        String startDate = DateUtil.format(tradeDateList.get(tradeDateList.size() - 1), "yyyyMMdd");
        if (!DateUtil.isSameDay(newestTradeDate, now)) {
            log.info("今日未开盘,日期===={}", today);
            return;
        }
        log.info("开始处理【{}】价格数据", today);
        //上锁，禁止查询
        try {

            redisUtil.valueSet("lock", 1);
            stockService.loadStockInfoToRedis();
            List<StockInfo> stockList = stockService.getStockList();

//            List<String> targetList = ListUtil.toList("002660", "002995", "000681", "000404", "002420");
//            stockList = stockList.stream()
//                    .filter(e -> targetList.contains(e.getStockCode())).collect(Collectors.toList());
            for (StockInfo stockInfo : stockList) {
                List<StockPriceHistory> priceList = stockService.fetchStockHistory(stockInfo, startDate, today);

                if (CollectionUtil.isEmpty(priceList)) {
                    continue;
                }
                int tag = 0;

                StockPriceHistory current = priceList.get(0);
                Date noteDate = current.getNoteDate();
                if (!DateUtil.isSameDay(now, noteDate)) {
                    continue;
                }
                BigDecimal priceDifferRate = current.getPriceDifferRate();
                if (surgedDown.compareTo(priceDifferRate) < 0 && surgedUp.compareTo(priceDifferRate) > 0) {
                    tag = tag | (1 << 1);
                } else if (declineDown.compareTo(priceDifferRate) < 0 && declineUp.compareTo(priceDifferRate) > 0) {
                    tag = tag | (1 << 2);
                }

                if (priceList.size() == 1) {
                    if (tag != 0) {
                        current.setTag(tag);
                        current.setCreateTime(Timestamp.from(Instant.now()));
                        stockService.saveSingleDailyData(current);
                    }
                    continue;
                }
                StockPriceHistory last = priceList.get(1);
                BigDecimal avgPricePast120Days = current.getAvgPricePast120Days();
                BigDecimal avgPricePast120DaysLast = last.getAvgPricePast120Days();
                if (avgPricePast120Days != null && avgPricePast120DaysLast != null) {
                    BigDecimal endPrice = current.getEndPrice();
                    BigDecimal endPriceLast = last.getEndPrice();
                    if (endPriceLast.compareTo(avgPricePast120DaysLast) < 0 && endPrice.compareTo(avgPricePast120Days) > 0) {
                        tag = tag | 1;
                    }
                }

                if (tag != 0) {
                    current.setTag(tag);
                    current.setCreateTime(Timestamp.from(Instant.now()));
                    stockService.saveSingleDailyData(current);
                }
            }

            statisticsService.marketStatistics(today);
        } finally {
            redisUtil.delete("lock");
            redisUtil.deleteFuzzy("stock:*");
        }

    }
}
