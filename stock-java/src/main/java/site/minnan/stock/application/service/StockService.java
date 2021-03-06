package site.minnan.stock.application.service;

import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.domain.vo.EligibleStockListVO;
import site.minnan.stock.domain.vo.KLineVO;
import site.minnan.stock.domain.vo.ListQueryVO;
import site.minnan.stock.infrastructure.exception.ProcessingException;
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
    List<StockPriceHistory> fetchStockHistory(StockInfo stockInfo, String startDate, String endDate);

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
    ListQueryVO<EligibleStockListVO> getEligibleStockList(GetEligibleStockListDTO dto) throws ProcessingException;

    /**
     * 查询K线图数据
     *
     * @param dto
     * @return
     */
    KLineVO getKLineData(DetailsQueryDTO dto);

    /**
     * 探测请求，探测今日有无开盘
     *
     * @param date
     * @return
     */
    boolean detected(String date);

    /**
     * 获取需要处理的股票
     *
     * @return
     */
    List<StockInfo> getStockList();

    /***
     * 批量添加价格数据
     * @param stockPriceHistoryList
     */
    void saveDailyData(List<StockPriceHistory> stockPriceHistoryList);

    /**
     * 插入单条每日数据
     *
     * @param stockPriceHistory
     */
    void saveSingleDailyData(StockPriceHistory stockPriceHistory);

    /**
     * 执行统计任务(计算任务)
     *
     * @param date
     */
    void calculate(String date);

    /**
     * 更新股票代码（更换TS源使用)
     *
     * @param stockInfo 股票信息
     * @return 更新成功返回True, 未找到股票返回False
     */
    boolean updateStockNickCode(StockInfo stockInfo);

    /**
     * 将股票信息加载带redis中（建立代码与id的映射）
     */
    void loadStockInfoToRedis();
}
