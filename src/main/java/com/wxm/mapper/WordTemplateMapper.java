package com.wxm.mapper;

import com.wxm.entity.WordEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
@Mapper
@Component(value = "wordTemplateMapper")
public interface WordTemplateMapper {
    @Select("select id,name,des,createTime,html from [WORD_TEMPLATE] where name = #{name}")
    WordEntity queryHtmlbyName(@Param("name")String name);

    @Select("select id,name,des,createTime,html from [WORD_TEMPLATE] where id = #{id}")
    WordEntity queryHtmlbyId(@Param("id")int id);

    @Select("select id,name,des,createTime,html from [WORD_TEMPLATE]")
    List<WordEntity> queryHtmlTemplate();

    @Insert("insert into [WORD_TEMPLATE](createTime,html) values(#{createTime},#{html})")
    int insert(@Param("createTime") Timestamp createTime, @Param("html") String html);

    @Insert("insert into [WORD_TEMPLATE](name,des,html) values(#{name},#{des},#{html})")
    int insertWordEntity(WordEntity wordEntity);

    @Delete("DELETE FROM [WORD_TEMPLATE] WHERE id =#{id}")
    void delete(int id);

    @Update("UPDATE [WORD_TEMPLATE] SET name=#{name},des=#{des},html=#{html} WHERE id=#{id}")
    void update(WordEntity wordEntity);

    @Select("select count(*) from [WORD_TEMPLATE]")
    int count();
}
