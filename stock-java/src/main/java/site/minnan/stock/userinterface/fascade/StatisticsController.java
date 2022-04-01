package site.minnan.stock.userinterface.fascade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.minnan.stock.application.service.StatisticsService;
import site.minnan.stock.domain.vo.MarketLimitLineVO;
import site.minnan.stock.infrastructure.exception.ProcessingException;
import site.minnan.stock.userinterface.response.ResponseEntity;

/**
 * 统计控制器
 *
 * @author Minnan on 2022/04/01
 */
@RestController
@RequestMapping("/stock/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 查询市场涨跌停数量折线图
     *
     * @return
     */
    @PostMapping("getMarketLimitLine")
    public ResponseEntity<MarketLimitLineVO> getMarketLimitLine() throws ProcessingException {
        MarketLimitLineVO vo = statisticsService.getMarketLimitData();
        return ResponseEntity.success(vo);
    }
}
