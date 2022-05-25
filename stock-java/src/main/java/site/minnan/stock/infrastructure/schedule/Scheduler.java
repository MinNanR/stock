package site.minnan.stock.infrastructure.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @Scheduled(cron = "30 15 0 * * *")
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
        redisUtil.valueSet("lock", 1);
        stockService.loadStockInfoToRedis();
        List<StockInfo> stockList = stockService.getStockList();
        List<StockPriceHistory> dataToInsert = new ArrayList<>();

        for (StockInfo stockInfo : stockList) {
            List<StockPriceHistory> priceList = stockService.fetchStockHistory(stockInfo, startDate, today);

            if (CollectionUtil.isEmpty(priceList)) {
                continue;
            }
            int tag = 0;

            StockPriceHistory current = priceList.get(0);
            StockPriceHistory last = priceList.get(1);
            BigDecimal priceDifferRate = current.getPriceDifferRate();
            if (surgedDown.compareTo(priceDifferRate) < 0 && surgedUp.compareTo(priceDifferRate) > 0) {
                tag = tag | (1 << 1);
            } else if (declineDown.compareTo(priceDifferRate) < 0 && declineUp.compareTo(priceDifferRate) > 0) {
                tag = tag | (1 << 2);
            }

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
                dataToInsert.add(current);
            }

            if (dataToInsert.size() > 800) {
                stockService.saveDailyData(dataToInsert);
                dataToInsert = new ArrayList<>();
            }
        }

        if (dataToInsert.size() > 0) {
            stockService.saveDailyData(dataToInsert);
        }

        statisticsService.marketStatistics(today);
        redisUtil.delete("lock");
        redisUtil.deleteFuzzy("stock:");

    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadPoolExecutor pool = ExecutorBuilder.create()
                .setCorePoolSize(3)
                .setMaxPoolSize(3)
                .build();
        ArrayList<Integer> list = ListUtil.toList(1, 2, 3, 4, 5);
        ArrayList<Future<Integer>> futures = new ArrayList<>();
        for (Integer i : list) {
            Future<Integer> future = pool.submit(() -> {
                for (int j = 0; j < 5; j++) {
                    System.out.println("Thread " + i + "---" + j);
                    ThreadUtil.sleep(3, TimeUnit.SECONDS);
                }
                return i;
            });
            futures.add(future);
        }
        for (Future<Integer> future : futures) {
            Integer index = future.get();
            System.out.println("task " + index + " has finished");
        }
        System.out.println("all tasks have finished");
        pool.shutdown();
    }
}
