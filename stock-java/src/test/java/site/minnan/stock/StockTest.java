package site.minnan.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.minnan.stock.application.service.StatisticsService;
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.aggregate.AuthUser;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.domain.mapper.AuthUserMapper;
import site.minnan.stock.domain.mapper.StockInfoMapper;
import site.minnan.stock.domain.mapper.StockPriceHistoryMapper;
import site.minnan.stock.infrastructure.schedule.Scheduler;
import site.minnan.stock.infrastructure.utils.RedisUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes = StockApplication.class)
@Slf4j
public class StockTest {

    @Autowired
    StockService stockService;

    @Autowired
    StockInfoMapper stockInfoMapper;

    @Autowired
    AuthUserMapper authUserMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    StockPriceHistoryMapper priceHistoryMapper;

    @Autowired
    private StatisticsService statisticsService;

    @Test
    public void testInitStockInfo() {
        String url = "https://api.doctorxiong.club/v1/stock/all";
        HttpResponse response = HttpUtil.createGet(url).execute();
        Console.log(response.getStatus());
        JSONObject responseJson = JSONUtil.parseObj(response.body());
        JSONArray data = responseJson.getJSONArray("data");
        List<StockInfo> stockInfoList = data.stream()
                .map(e -> (JSONArray) e)
                .map(e -> {
                    StockInfo stockInfo = new StockInfo();
                    String stockNickCode = e.getStr(0);
                    stockInfo.setStockName(e.getStr(1));
                    stockInfo.setStockNickCode(stockNickCode);
                    String stockCode = ReUtil.findAllGroup0("\\d+", stockNickCode).get(0);
                    stockInfo.setStockCode(stockCode);
                    stockInfo.setDetected(1);
                    return stockInfo;
                })
                .collect(Collectors.toList());
        List<List<StockInfo>> split = CollUtil.split(stockInfoList, 500);
        split.forEach(list -> stockService.addStockBatch(list));
    }

    @Test
    public void testInitStockHistory() throws IOException {
        LambdaQueryWrapper<StockInfo> query = Wrappers.<StockInfo>lambdaQuery()
                .eq(StockInfo::getDetected, 1);
        query.last(" and id not in (select distinct stock_id from stock_price_history)");
        List<StockInfo> all = stockInfoMapper.selectList(query);

        List<List<StockInfo>> splitList = CollUtil.split(all, 500);
        int size = splitList.size();
        for (int i = 0; i < size; i++) {
            List<StockInfo> list = splitList.get(i);
            BufferedOutputStream os = FileUtil.getOutputStream(FileUtil.touch("F:\\Minnan\\stock\\stock-java" +
                    "\\logs\\insert_" + i + ".sql"));
            for (StockInfo stockInfo : list) {
                stockService.initStockPrice(stockInfo, os);
            }
            log.info("构建第{}段sql成功", i);
            os.close();
        }
    }


    @Autowired
    private Digester passwordEncoder;

    @Test
    public void testCreateUser() {
        String password = "657b298b04e033810343842f993c9817";
        String encodedPassword = passwordEncoder.digestHex(password);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        AuthUser createUser = authUserMapper.selectById(3);
        AuthUser authUser = AuthUser.builder()
                .username("leo")
                .password(encodedPassword)
                .passwordStamp(uuid)
                .realName("Leo")
                .build();

        authUserMapper.insert(authUser);
    }

    @Test
    public void testInitTushareStockInfo() {
        HttpRequest request = HttpUtil.createPost("http://localhost:8151/getAllStock");
        HttpResponse response = request.execute();
        String responseString = response.body();
        JSONArray stockArray = JSONUtil.parseArray(responseString);
        Map<Boolean, List<StockInfo>> haveRecord = Stream.iterate(0, i -> i + 1)
                .limit(stockArray.size())
                .map(stockArray::getJSONObject)
                .map(e -> StockInfo.builder()
                        .stockCode(e.getStr("symbol"))
                        .stockNickCode(e.getStr("ts_code"))
                        .stockName(e.getStr("name"))
                        .build())
                .collect(Collectors.groupingBy(e -> stockService.updateStockNickCode(e)));
        List<StockInfo> notInDb = haveRecord.get(false);
        stockService.addStockBatch(notInDb);
    }

    @Test
    public void testUpdatePrice() {
        stockService.loadStockInfoToRedis();
        HttpRequest request = HttpUtil.createPost("http://localhost:8151/getAllStock");
        HttpResponse response = request.execute();
        String responseString = response.body();
        JSONArray stockArray = JSONUtil.parseArray(responseString);
        List<StockInfo> stockInfoList = Stream.iterate(0, i -> i + 1)
                .limit(stockArray.size())
                .map(stockArray::getJSONObject)
                .map(e -> StockInfo.builder()
                        .stockCode(e.getStr("symbol"))
                        .stockNickCode(e.getStr("ts_code"))
                        .stockName(e.getStr("name"))
                        .build())
                .collect(Collectors.toList());
        List<List<StockInfo>> split = CollectionUtil.split(stockInfoList, 300);
        for (List<StockInfo> list : split) {
            List<StockPriceHistory> dataToInsert = new ArrayList<>();
            for (StockInfo stockInfo : list) {
                List<StockPriceHistory> result = stockService.fetchStockHistory(stockInfo, "20220517", "20220519");
                if (result != null) {
                    dataToInsert.addAll(result);
                }
            }
            stockService.saveDailyData(dataToInsert);
        }
    }

    @Test
    public void testCaculate() {
        DateTime today = DateUtil.beginOfDay(DateTime.now());
        DateTime date = DateUtil.beginOfYear(today);
        while (date.isBefore(today)) {
            stockService.calculate(date.toString("yyyy-MM-dd"));
            statisticsService.marketStatistics(date.toString("yyyy-MM-dd"));
            date.offset(DateField.DAY_OF_YEAR, 1);
        }
    }

    @Autowired
    Scheduler scheduler;

    @Test
    public void testDailyTask() {
        scheduler.getTodayPrice();
    }

    @Test
    public void testWashData() {
        List<StockInfo> stockList = stockService.getStockList();
        Optional<StockInfo> opt = stockList.stream().filter(e -> e.getStockCode().equals("688286")).findFirst();
        if (opt.isPresent()) {
            int i = stockList.indexOf(opt.get());
            stockList = stockList.subList(i, stockList.size());
        }

        List<StockPriceHistory> dataToInsert = new ArrayList<>();

        BigDecimal surgedUp = new BigDecimal("0.101");
        BigDecimal surgedDown = new BigDecimal("0.098");
        BigDecimal declineUp = new BigDecimal("-0.098");
        BigDecimal declineDown = new BigDecimal("-0.101");

        for (StockInfo stockInfo : stockList) {
            List<StockPriceHistory> priceList = stockService.fetchStockHistory(stockInfo, "20050101",
                    "20220524");

            int size = priceList.size();
            if(size == 0 ){
                continue;
            }
            for (int i = 0; i < size - 1; i++) {
                int tag = 0;
                StockPriceHistory current = priceList.get(i);
                StockPriceHistory last = priceList.get(i + 1);

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
            }

            int tag = 0;
            StockPriceHistory lastItem = priceList.get(size - 1);
            BigDecimal priceDifferRate = lastItem.getPriceDifferRate();
            if (surgedDown.compareTo(priceDifferRate) > 0 && surgedUp.compareTo(priceDifferRate) <= 0) {
                tag = tag | (1 << 1);
            } else if (declineDown.compareTo(priceDifferRate) > 0 && declineUp.compareTo(priceDifferRate) <= 0) {
                tag = tag | (1 << 2);
            }
            if (tag != 0) {
                lastItem.setTag(tag);
                dataToInsert.add(lastItem);
            }

            if (dataToInsert.size() > 800) {
                stockService.saveDailyData(dataToInsert);
                dataToInsert = new ArrayList<>();
            }
        }

        if(dataToInsert.size() > 0){
            stockService.saveDailyData(dataToInsert);
        }
    }
}
