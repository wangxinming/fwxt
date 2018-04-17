package com.wxm;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.mapper.OAUserMapper;
import com.wxm.model.OAAudit;
import com.wxm.model.OAUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMapperTest {
    @Autowired
    private OAUserMapper UserMapper;
    @Autowired
    private OAAuditMapper oaAuditMapper;

    @Test
    public void TestOAAudit(){
        OAAudit oaAudit = new OAAudit();
        oaAudit.setContent("删除用户");
        oaAudit.setCreateTime(new Date());
        oaAudit.setUserName("wang");
        oaAuditMapper.insertSelective(oaAudit);
//        List<OAAudit> oaAuditList =  oaAuditMapper.list(0,20);
//        int i = 0;
    }
    @Test
    public void testInsert() throws Exception {
        OAUser oa = new OAUser("a123456");
        UserMapper.insertSelective(oa);
        UserMapper.insertSelective(new OAUser("b123457"));
        UserMapper.insertSelective(new OAUser("c123458"));

        Assert.assertEquals(1, UserMapper.selectByPrimaryKey(1));
    }

    @Test
    public void testQuery() throws Exception {
//        List<UserTestEntity> users = UserMapper.getAll();
//        System.out.println(users.toString());
    }

    @Test
    public void testUpdate() throws Exception {
//        UserTestEntity user = UserMapper.getOne(4);
//        System.out.println(user.toString());
//        user.setAge(100);
//        user.setName("neo");
//        UserMapper.update(user);
//        Assert.assertTrue(("neo".equals(UserMapper.getOne(3).getName())));
    }
}
