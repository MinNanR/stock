package site.minnan.stock.application.service;

import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.domain.vo.EligibleStockListVO;
import site.minnan.stock.domain.vo.KLineVO;
import site.minnan.stock.domain.vo.ListQueryVO;
import site.minnan.stock.userinterface.dto.DetailsQueryDTO;
import site.minnan.stock.userinterface.dto.GetEligibleStockListDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 股票服务
 *
 * @author Minnan on 2022/03/24
 */
public interface StockService {

    /**
     * 添加股票
     *
     * @param stock
     */
    void addStock(StockInfo stock);

    /**
     * 批量添加股票
     *
     * @param stockInfoList
     */
    void addStockBatch(List<StockInfo> stockInfoList);

    /**
     * 查询历史股票数据
     *
     * @param stockInfo 股票信息
     * @param startDate 开始日期
     * @return
     */
    List<StockPriceHistory> fetchStockHistory(StockInfo stockInfo, String startDate);

    /**
     * 初始化股票价格
     *
     * @param stockInfo
     */
    void initStockPrice(StockInfo stockInfo, OutputStream outputStream) throws IOException;

    /**
     * 查询符合条件的股票
     *
     * @param dto
     * @return
     */
    ListQueryVO<EligibleStockListVO> getEligibleStockList(GetEligibleStockListDTO dto) throws Exception;

    /**
     * 查询K线图数据
     *
     * @param dto
     * @return
     */
    KLineVO getKLineData(DetailsQueryDTO dto);
}
