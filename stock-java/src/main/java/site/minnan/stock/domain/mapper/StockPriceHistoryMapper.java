package site.minnan.stock.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import site.minnan.stock.domain.entity.StockPriceHistory;

import java.util.List;

/**
 *
 *
 * @author Minnan on 2022/03/28
 */
@Repository
@Mapper
public interface StockPriceHistoryMapper extends BaseMapper<StockPriceHistory> {

    @Select("select distinct stock_code from stock_price_history where note_date = '2022-05-24'")
    List<String> getExistStock();
}
