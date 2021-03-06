package site.minnan.stock.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import site.minnan.stock.domain.aggregate.StockInfo;
import site.minnan.stock.domain.entity.StockPriceHistory;
import site.minnan.stock.domain.vo.EligibleStockListVO;
import site.minnan.stock.userinterface.dto.GetEligibleStockListDTO;

import java.util.List;

/**
 * @author Minnan on 2022/03/24
 */
@Mapper
@Repository
public interface StockInfoMapper extends BaseMapper<StockInfo> {

    /**
     * 批量添加股票信息
     *
     * @param list
     */
    void addStockInfoBatch(List<StockInfo> list);

    /**
     * 批量添加股票历史数据
     *
     * @param list
     * @return
     */
    int insertStockPriceHistoryBatch(List<StockPriceHistory> list);

    /**
     * 插入单条历史数据
     * @param stockPriceHistory
     */
    void insertStockPriceHistory(StockPriceHistory stockPriceHistory);


    /**
     * 查询符合条件的股票参数
     *
     * @param dto
     * @return
     */
    List<EligibleStockListVO> getEligibleStockList(GetEligibleStockListDTO dto);

    /**
     * 计算符合条件的股票数量
     *
     * @param dto
     * @return
     */
    Integer countEligibleStock(GetEligibleStockListDTO dto);

    /**
     * 调用存储过程，计算均价涨跌幅等数据
     *
     * @param date
     */
    @Select("call calculate_avg_price(#{date})")
    void callProcedureCalculateAvgPrice(String date);
}
