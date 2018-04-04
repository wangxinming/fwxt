package com.wxm.service;

import com.wxm.entity.WordEntity;
import com.wxm.mapper.WordTemplateMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class WordTemplateService {
    @Autowired
    private WordTemplateMapper wordTemplate;

    public WordEntity queryHtmlbyName(String name){
        return wordTemplate.queryHtmlbyName(name);
    }
    public WordEntity queryHtmlbyId(int id){
        return wordTemplate.queryHtmlbyId(id);
    }
    public List<WordEntity> queryHtmlTemplate(){
        return wordTemplate.queryHtmlTemplate();
    }
    public int insert(Timestamp timeStamp, String html){
        return wordTemplate.insert(timeStamp,html);
    }
    public int insert(WordEntity wordEntity){
        return wordTemplate.insertWordEntity(wordEntity);
    }
    public void delete(int id){
        wordTemplate.delete(id);
    }
    public void update(WordEntity wordEntity){
        wordTemplate.update(wordEntity);
    }
    public int count(){
        return wordTemplate.count();
    }
}
