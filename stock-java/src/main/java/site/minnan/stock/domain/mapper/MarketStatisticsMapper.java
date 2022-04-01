package site.minnan.stock.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import site.minnan.stock.domain.entity.MarketStatistics;

import java.util.List;

/**
 * 市场统计查询
 *
 * @author  on 2022/04/01
 */
@Mapper
@Repository
public interface MarketStatisticsMapper extends BaseMapper<MarketStatistics> {

    @Select("call market_statistics(#{date})")
    void callProcedureMarketStatistics(String date);

    @Select("select id id, note_date noteDate, surged_limit_count surgedLimitCount, decline_limit_count " +
            "declineLimitCount from market_statistics order by note_date;")
    List<MarketStatistics> selectAll();
}
