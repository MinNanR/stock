package site.minnan.stock.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.domain.mapper.StockInfoMapper;
import site.minnan.stock.domain.mapper.StockPriceHistoryMapper;
import site.minnan.stock.domain.vo.EligibleStockListVO;
import site.minnan.stock.domain.vo.KLineVO;
import site.minnan.stock.domain.vo.ListQueryVO;
import site.minnan.stock.infrastructure.utils.RedisUtil;
import site.minnan.stock.userinterface.dto.DetailsQueryDTO;
import site.minnan.stock.userinterface.dto.GetEligibleStockListDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiFunction;
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

    @Autowired
    private StockPriceHistoryMapper stockPriceHistoryMapper;

    @Value("${stock.priceUrl}")
    private String PRICE_URL;

    @Value("${stock.token}")
    private String TOKEN;

    @Autowired
    private RedisUtil redisUtil;

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
    public List<StockPriceHistory> fetchStockHistory(StockInfo stockInfo, String startDate, String endDate) {
        String code = stockInfo.getStockCode();
        Integer stockId = stockInfo.getId();
        String url = StrUtil.format("{}?token={}&code={}&startDate={}&endDate={}&type=1", PRICE_URL, TOKEN, code,
                startDate, endDate);
        HttpResponse response = HttpUtil.createGet(url).execute();
        if (!response.isOk()) {
            return null;
        }
        JSONObject responseJson = JSONUtil.parseObj(response.body());
        log.info("{}:{}", code, responseJson);
        if (responseJson.getInt("code") != 200) {
            return null;
        }
        JSONArray data = responseJson.getJSONArray("data");
        return data.stream()
                .map(e -> (JSONArray) e)
                .map(item -> StockPriceHistory.builder()
                        .stockId(stockId)
                        .stockCode(code)
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
     * @param stockInfo 股票信息
     */
    @Override
    @Transactional
    public void initStockPrice(StockInfo stockInfo, OutputStream os) throws IOException {
        String today = DateUtil.today();
        List<StockPriceHistory> priceHistoryList = fetchStockHistory(stockInfo, "2021-01-01", today);
        if (CollUtil.isEmpty(priceHistoryList)) {
            stockInfo.setDetected(0);
            stockInfoMapper.updateById(stockInfo);
            return;
        }
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
        String sql = "insert into stock_price_history (stock_id, stock_code, start_price, end_price, highest_price, " +
                "lowest_price, volume, end_price_last, avg_price_past_120_days, avg_price_past_120_days_last, " +
                "note_date, create_time) VALUES ";
        String values = priceHistoryList.stream()
                .map(e -> StrUtil.format("({},{},{},{},{},{},{},{},{},{},'{}', now())",
                        e.getStockId(), e.getStockCode(), e.getStartPrice(), e.getEndPrice(), e.getHighestPrice(),
                        e.getLowestPrice(), e.getVolume(), e.getEndPriceLast(), e.getAvgPricePast120Days(),
                        e.getAvgPricePast120DaysLast(),
                        DateUtil.format(e.getNoteDate(), "yyyy-MM-dd")))
                .collect(Collectors.joining(","));
        os.write((sql + values + ";").getBytes());
        os.write('\n');
//        int i = stockInfoMapper.insertStockPriceHistoryBatch(priceHistoryList);
//        if (i > 0) {
//            log.info("保存【{}】数据成功，条数==={}", stockInfo.getStockCode(), priceHistoryList.size());
//        } else {
//            log.info("保存【{}】数据失败", stockInfo.getStockCode());
//        }
//        redisUtil.hashPut("stock_price", stockInfo.getStockCode(), priceHistoryList);
////        log.info(JSONUtil.toJsonPrettyStr(priceHistoryList));
    }

    /**
     * 查询符合条件的股票
     *
     * @param dto
     * @return
     */
    @Override
    public ListQueryVO<EligibleStockListVO> getEligibleStockList(GetEligibleStockListDTO dto) throws Exception {
        Object lock = redisUtil.getValue("lock");
        if (lock != null) {
            throw new Exception("数据统计中");
        }
        Integer totalCount = stockInfoMapper.countEligibleStock(dto);
        List<EligibleStockListVO> list = totalCount > 0 ?
                stockInfoMapper.getEligibleStockList(dto) :
                ListUtil.empty();
        return new ListQueryVO<>(list, totalCount);
    }

    /**
     * 查询K线图数据
     *
     * @param dto
     * @return
     */
    @Override
    public KLineVO getKLineData(DetailsQueryDTO dto) {
//        LambdaQueryWrapper<StockPriceHistory> query = Wrappers.<StockPriceHistory>lambdaQuery()
//                .eq(StockPriceHistory::getStockId, dto.getId())
//                .orderByAsc(StockPriceHistory::getNoteDate);
        QueryWrapper<StockPriceHistory> query = new QueryWrapper<>();
        query.eq("stock_id", dto.getId())
                .orderByAsc("note_date");
        List<StockPriceHistory> rawData = stockPriceHistoryMapper.selectList(query);
        KLineVO vo = new KLineVO();
        rawData.forEach(vo::add);
        return vo;
    }

    /**
     * 探测请求，探测今日有无开盘
     *
     * @param date
     * @return
     */
    @Override
    public boolean detected(String date) {
        String url = StrUtil.format("https://api.doctorxiong.club/v1/stock/kline/day?token={}&code=sh000001" +
                "&startDate={}&endDate={}&type=1", TOKEN, date, date);
        String responseString = HttpUtil.createGet(url)
                .execute()
                .body();
        JSONObject responseJson = JSONUtil.parseObj(responseString);
        return responseJson.getInt("code") == 200;
    }

    /**
     * 获取需要处理的股票
     *
     * @param start 偏移量
     * @return
     */
    @Override
    public List<StockInfo> getStockList(Integer start) {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","stock_code", "stock_nick_code")
                .eq("detected", 1)
                .last(" limit " + start + ", 500");
        return stockInfoMapper.selectList(queryWrapper);
    }

    /***
     * 批量添加价格数据
     * @param stockPriceHistoryList
     */
    @Override
    @Transactional
    public void saveDailyData(List<StockPriceHistory> stockPriceHistoryList) {
        stockInfoMapper.insertStockPriceHistoryBatch(stockPriceHistoryList);
        log.info("success to save history, {} row affected", stockPriceHistoryList.size());
    }

    /**
     * 执行统计任务(计算任务
     *
     * @param date
     */
    @Override
    @Transactional
    public void calculate(String date) {
        stockInfoMapper.callProcedureCalculateAvgPrice(date);
    }
}
