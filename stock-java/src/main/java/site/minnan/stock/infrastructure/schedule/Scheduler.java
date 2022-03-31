package site.minnan.stock.infrastructure.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.infrastructure.utils.RedisUtil;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Scheduler {

    @Autowired
    private StockService stockService;

    @Autowired
    private RedisUtil redisUtil;

    @Scheduled(cron = "30 15 0 * * *")
    public void getTodayPrice() {
        String today = "2022-03-30";
        if (!stockService.detected(today)) {
            log.info("今日未开盘,日期===={}", today);
            return;
        }
        log.info("开始处理【{}】价格数据", today);
        //上锁，禁止查询
        redisUtil.valueSet("lock", 1);
        int start = 0;
        while (true) {
            List<StockInfo> stockList = stockService.getStockList(start);
            if (CollUtil.isEmpty(stockList)) {
                break;
            }
            List<StockPriceHistory> dataToInsert = new ArrayList<>();
            for (StockInfo stockInfo : stockList) {
                List<StockPriceHistory> result = stockService.fetchStockHistory(stockInfo, today, today);
                if (result != null) {
                    dataToInsert.addAll(result);
                }
            }
            stockService.saveDailyData(dataToInsert);
            start = start + 500;
        }
        stockService.calculate(today);
        redisUtil.delete("lock");
    }
}
