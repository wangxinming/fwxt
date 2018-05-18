//package com.wxm.mapper;
//
//import com.wxm.entity.WordEntity;
//import org.apache.ibatis.annotations.*;
//import org.springframework.stereotype.Component;
//
//import java.sql.Timestamp;
//import java.util.Date;
//import java.util.List;
//@Mapper
//@Component(value = "wordTemplateMapper")
//public interface WordTemplateMapper {
//    @Select("select id,name,des,createTime,html from [WORD_TEMPLATE] where name = #{name}")
//    WordEntity queryHtmlbyName(@Param("name")String name);
//
//    @Select("select id,name from [WORD_TEMPLATE] where id = (select tem_ID from [Deployment_Tep_Rel] where dep_ID = #{dID})")
//    WordEntity queryInfoRel(@Param("dID")String dID);
//
//    @Select("select count(*) from [Deployment_Tep_Rel] where dep_ID=#{dID}")
//    int coutRelByDID(@Param("dID")String dID);
//
//
//    @Update("UPDATE [Deployment_Tep_Rel] SET tem_ID=#{rID} WHERE dep_ID=#{dID}")
//    int updateRelation(@Param("dID")String dID,@Param("rID")int rID);
//
//    @Insert("insert into [Deployment_Tep_Rel](dep_ID,tem_ID) values(#{dID},#{rID})")
//    int insertRel(@Param("dID")String dID,@Param("rID")int rID);
//
//    @Delete("delete from [Deployment_Tep_Rel] where id=#{id}")
//    int removeRelation(@Param("id")int id);
//
//    @Delete("delete from [Deployment_Tep_Rel] where tem_ID=#{id}")
//    int removeRelationByTmpId(@Param("id")int id);
//
//    @Select("select id,name,des,createTime,html from [WORD_TEMPLATE] where id = #{id}")
//    WordEntity queryHtmlbyId(@Param("id")int id);
//
//    @Select("select id,name,des,createTime,html from [WORD_TEMPLATE]")
//    List<WordEntity> queryHtmlTemplate();
//
//    @Insert("insert into [WORD_TEMPLATE](createTime,html) values(#{createTime},#{html})")
//    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
//    int insert(WordEntity wordEntity);
//
////    @Insert("insert into [WORD_TEMPLATE](name,des,html) values(#{name},#{des},#{html})")
////    int insertWordEntity(WordEntity wordEntity);
//
//    @Delete("DELETE FROM [WORD_TEMPLATE] WHERE id =#{id}")
//    void delete(int id);
//
//    @Update("UPDATE [WORD_TEMPLATE] SET name=#{name},des=#{des},html=#{html} WHERE id=#{id}")
//    void update(WordEntity wordEntity);
//
//    @Update("UPDATE [WORD_TEMPLATE] SET html=#{html} WHERE id=#{id}")
//    void updateHtml(WordEntity wordEntity);
//
//    @Select("select count(*) from [WORD_TEMPLATE]")
//    int count();
//}
