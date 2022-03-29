package site.minnan.stock.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import site.minnan.stock.domain.entity.StockPriceHistory;

/**
 *
 *
 * @author Minnan on 2022/03/28
 */
@Repository
@Mapper
public interface StockPriceHistoryMapper extends BaseMapper<StockPriceHistory> {
}
