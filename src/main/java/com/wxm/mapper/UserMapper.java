package com.wxm.mapper;

import com.wxm.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component(value = "userMapper")
public interface UserMapper {
    @Select("select * from [user] where name = #{name}")
    User findUserByName(@Param("name")String name);
}
