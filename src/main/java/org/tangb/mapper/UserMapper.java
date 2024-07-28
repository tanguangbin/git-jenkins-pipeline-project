package org.tangb.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tangb.modules.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    int updateNicknameById(@Param("ew") LambdaQueryWrapper<User> lambdaQueryWrapper,@Param("str") String str);
}
