package com.wxm.service;

import com.wxm.entity.WordTemplateField;
import com.wxm.mapper.WordTemplateFieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordTemplateFieldService {
    @Autowired
    private WordTemplateFieldMapper wordTemplateFieldMapper;

    public int countTotal(){
        return wordTemplateFieldMapper.countTotal();
    }

    public int countByTemplateId(int templateId){
        return wordTemplateFieldMapper.countByTemplateId(templateId);
    }

    public List<WordTemplateField> getWordTemplateFieldByTemplateId(int templateId){
        return wordTemplateFieldMapper.getWordTemplateFieldByTemplateId(templateId);
    }

    public List<WordTemplateField> getWordTemplateField(){
        return wordTemplateFieldMapper.getWordTemplateField();
    }
    public int insert(WordTemplateField wordTemplateField){
        return wordTemplateFieldMapper.insert(wordTemplateField);
    }

    public int update(WordTemplateField wordTemplateField){
        return wordTemplateFieldMapper.update(wordTemplateField);
    }

    public int delete(int id){
        return wordTemplateFieldMapper.delete(id);
    }
}
