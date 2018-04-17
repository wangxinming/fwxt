package com.wxm.mapper;

import com.wxm.entity.WordTemplateField;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "wordTemplateFieldMapper")
public interface WordTemplateFieldMapper {

    @Select("select count(*) from [WORD_TEMPLATE_FIELD]")
    int countTotal();

    @Select("select count(*) from [WORD_TEMPLATE_FIELD] where templateId = #{templateId}")
    int countByTemplateId(int templateId);

    @Select("select id,templateId,fieldMd5,field,type,length,start from [WORD_TEMPLATE_FIELD] where templateId = #{templateId} ")
    List<WordTemplateField> getWordTemplateFieldByTemplateId(int templateId);

    @Select("select id,templateId,fieldMd5,field,type,length,start from [WORD_TEMPLATE_FIELD] ")
    List<WordTemplateField> getWordTemplateField();

    @Insert("insert into [WORD_TEMPLATE_FIELD](templateId,fieldMd5,field,type,length,start) values(#{templateId},#{fieldMd5},#{field},#{type},#{length},#{start})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(WordTemplateField wordTemplateField);

    @Update("UPDATE [WORD_TEMPLATE_FIELD] SET type=#{type},length=#{length} WHERE id=#{id}")
    int update(WordTemplateField wordTemplateField);

    @Delete("DELETE FROM [WORD_TEMPLATE_FIELD] WHERE id =#{id}")
    int delete(int id);
}
