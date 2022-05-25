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
import java.util.Date;
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

        Map<String, BigDecimal> sh000001 = getMarketLineData("sh000001");
        Map<String, BigDecimal> sh000016 = getMarketLineData("sh000016");
        Map<String, BigDecimal> sh000905 = getMarketLineData("sh000905");
        Map<String, BigDecimal> sz399300 = getMarketLineData("sz399300");

        list.forEach(e -> {
            String noteDate = DateUtil.format(e.getNoteDate(), "yyyy-MM-dd");
            vo.add(e, sh000001.get(noteDate), sh000016.get(noteDate), sh000905.get(noteDate), sz399300.get(noteDate));
        });

        return vo;
    }

    private Map<String, BigDecimal> getMarketLineData(String code){
        String marketDataUrl = "https://api.doctorxiong.club/v1/stock/kline/day?startDate=2005-01-01&type=1&code=" + code;
        HttpResponse response = HttpUtil.createGet(marketDataUrl).execute();
        String responseString = response.body();
        JSONObject responseJson = JSONUtil.parseObj(responseString);
        JSONArray data = responseJson.getJSONArray("data");
        return data.stream()
                .map(e -> (JSONArray) e)
                .collect(Collectors.toMap(e -> e.getStr(0), e -> e.getBigDecimal(2)));
    }

    /**
     * 获取过去120个交易日
     *
     * @return
     */
    @Override
    public List<Date> getTradeDateListPast120() {
        return marketStatisticsMapper.getTradeDatePast120();
    }
}
