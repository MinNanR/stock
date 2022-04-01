package site.minnan.stock.application.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.minnan.stock.application.service.StatisticsService;
import site.minnan.stock.domain.entity.MarketStatistics;
import site.minnan.stock.domain.mapper.MarketStatisticsMapper;
import site.minnan.stock.domain.vo.MarketLimitLineVO;
import site.minnan.stock.infrastructure.exception.ProcessingException;
import site.minnan.stock.infrastructure.utils.RedisUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Minnan on 2022/04/01
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private MarketStatisticsMapper marketStatisticsMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 市场统计任务
     *
     * @param date
     */
    @Override
    @Transactional
    public void marketStatistics(String date) {
        marketStatisticsMapper.callProcedureMarketStatistics(date);
    }

    /**
     * 查询市场涨停跌停数量数据
     *
     * @return
     */
    @Override
    public MarketLimitLineVO getMarketLimitData() throws ProcessingException {
        Object lock = redisUtil.getValue("lock");
        if (lock != null) {
            throw new ProcessingException("数据统计中");
        }
        List<MarketStatistics> list = marketStatisticsMapper.selectAll();
        MarketLimitLineVO vo = new MarketLimitLineVO();
        String marketDataUrl = "https://api.doctorxiong.club/v1/stock/kline/day?startDate=2021-01-01" +
                "&type=1&code=sh000001";
        HttpResponse response = HttpUtil.createGet(marketDataUrl).execute();
        String responseString = response.body();
        JSONObject responseJson = JSONUtil.parseObj(responseString);
        JSONArray data = responseJson.getJSONArray("data");
        Map<String, BigDecimal> dateMarketMap = data.stream()
                .map(e -> (JSONArray) e)
                .collect(Collectors.toMap(e -> e.getStr(0), e -> e.getBigDecimal(2)));
        list.forEach(e -> vo.add(e, dateMarketMap.get(DateUtil.format(e.getNoteDate(), "yyyy-MM-dd"))));
        return vo;
    }
}
