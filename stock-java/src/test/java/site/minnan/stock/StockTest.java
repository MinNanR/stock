package site.minnan.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.digest.MD5;
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
import site.minnan.stock.application.service.StockService;
import site.minnan.stock.domain.aggregate.AuthUser;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.mapper.AuthUserMapper;
import site.minnan.stock.domain.mapper.StockInfoMapper;
import site.minnan.stock.infrastructure.utils.RedisUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void testCreateUser(){
        String password = "c663b11dff4be0badcf652212a2c1102";
        String encodedPassword = passwordEncoder.digestHex(password);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        AuthUser authUser = AuthUser.builder()
                .username("min")
                .password(encodedPassword)
                .passwordStamp(uuid)
                .realName("民难")
                .build();

        authUserMapper.insert(authUser);

    }
}
