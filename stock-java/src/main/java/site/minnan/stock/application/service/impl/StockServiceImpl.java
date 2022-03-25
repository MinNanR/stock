package site.minnan.stock.application.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.domain.mapper.StockInfoMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 股票服务
 *
 * @author Minnan on 2022/03/24
 */
@Service
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Value("${stock.priceUrl}")
    private String PRICE_URL;

    @Value("${stock.token}")
    private String TOKEN;

    /**
     * 添加股票
     *
     * @param stock
     */
    @Override
    public void addStock(StockInfo stock) {
        stockInfoMapper.insert(stock);
    }

    /**
     * 批量添加股票
     *
     * @param stockInfoList
     */
    @Override
    public void addStockBatch(List<StockInfo> stockInfoList) {
        stockInfoMapper.addStockInfoBatch(stockInfoList);
    }

    /**
     * 查询历史股票数据
     *
     * @param stockInfo 股票信息
     * @param startDate 开始日期
     * @return
     */
    @Override
    public List<StockPriceHistory> fetchStockHistory(StockInfo stockInfo, String startDate) {
        String code = stockInfo.getStockCode();
        Integer stockId = stockInfo.getId();
        String url = StrUtil.format("{}?token={}&code={}&startDate={}&type=1", PRICE_URL, TOKEN, code, startDate);
        HttpResponse response = HttpUtil.createGet(url).execute();
        if (!response.isOk()) {
            return null;
        }
        JSONObject responseJson = JSONUtil.parseObj(response.body());
        if (responseJson.getInt("code") != 200) {
            return null;
        }
        JSONArray data = responseJson.getJSONArray("data");
        return data.stream()
                .map(e -> (JSONArray) e)
                .map(item -> StockPriceHistory.builder()
                        .stockId(stockId)
                        .startPrice(item.getBigDecimal(1))
                        .endPrice(item.getBigDecimal(2))
                        .highestPrice(item.getBigDecimal(3))
                        .lowestPrice(item.getBigDecimal(4))
                        .volume(NumberUtil.parseInt(item.getStr(5)))
                        .noteDate(DateUtil.parse(item.getStr(0), "yyyy-MM-dd"))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 初始化股票价格
     *
     * @param priceHistoryList
     */
    @Override
    @Transactional
    public void initStockPrice(List<StockPriceHistory> priceHistoryList) {
        Iterator<StockPriceHistory> itr = priceHistoryList.iterator();
        Queue<BigDecimal> past120 = priceHistoryList.stream()
                .limit(119)
                .peek(e -> {
                    itr.next();
                    e.setAvgPricePast120Days(null);
                    e.setAvgPricePast120DaysLast(null);
                })
                .map(StockPriceHistory::getEndPrice)
                .collect(Collectors.toCollection(LinkedList::new));
        BigDecimal count = new BigDecimal(120);
        BigDecimal lastAvgPrice = null;
        while (itr.hasNext()) {
            StockPriceHistory item = itr.next();
            past120.add(item.getEndPrice());
            BigDecimal avgPrice = past120.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO).divide(count, 4,
                    RoundingMode.HALF_UP);
            item.setAvgPricePast120DaysLast(lastAvgPrice);
            item.setAvgPricePast120Days(avgPrice);
            lastAvgPrice = avgPrice;
            past120.poll();
        }
        int size = priceHistoryList.size();
        for (int i = 1; i < size; i++) {
            priceHistoryList.get(i).setEndPriceLast(priceHistoryList.get(i - 1).getEndPrice());
        }
        int i = stockInfoMapper.insertStockPriceHistoryBatch(priceHistoryList);
//        log.info(JSONUtil.toJsonPrettyStr(priceHistoryList));
    }
}
