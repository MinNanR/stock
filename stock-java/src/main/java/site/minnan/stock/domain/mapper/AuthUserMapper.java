package site.minnan.stock.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import site.minnan.stock.domain.aggregate.AuthUser;

/**
 *
 * @author Minnan on 2021/12/31
 */
@Mapper
@Repository
public interface AuthUserMapper extends BaseMapper<AuthUser> {
}
