package site.minnan.stock.infrastructure.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
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

import java.util.ArrayList;
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

//    @Scheduled(cron = "30 15 0 * * *")
    public void getTodayPrice() {
        String today = DateUtil.today().replaceAll("-", "");
        if (!stockService.detected(today)) {
            log.info("今日未开盘,日期===={}", today);
            return;
        }
        log.info("开始处理【{}】价格数据", today);
        //上锁，禁止查询
        redisUtil.valueSet("lock", 1);
        stockService.loadStockInfoToRedis();
        List<StockInfo> stockList = stockService.getStockList();
        List<List<StockInfo>> split = CollectionUtil.split(stockList, 500);
        for (List<StockInfo> stockInfos : split) {
            List<StockPriceHistory> dataToInsert = new ArrayList<>();
            for (StockInfo stockInfo : stockInfos) {
                List<StockPriceHistory> result = stockService.fetchStockHistory(stockInfo, today
                        , today);
                if (CollectionUtil.isNotEmpty(result)) {
                    dataToInsert.addAll(result);
                }
            }
            stockService.saveDailyData(dataToInsert);
        }
//        try {
//            for (Future<Integer> future : taskList) {
//                future.get();
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            log.info("日常统计任务u异常", e);
//        }
//
//        pool.shutdown();

//        int start = 0;
//        while (true) {
//            List<StockInfo> stockList = stockService.getStockList(start);
//            if (CollUtil.isEmpty(stockList)) {
//                break;
//            }
//            List<StockPriceHistory> dataToInsert = new ArrayList<>();
//            for (StockInfo stockInfo : stockList) {
//                List<StockPriceHistory> result = stockService.fetchStockHistory(stockInfo, today, today);
//                if (result != null) {
//                    dataToInsert.addAll(result);
//                }
//            }
//            stockService.saveDailyData(dataToInsert);
//            start = start + 500;
//        }
        stockService.calculate(today);
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
