package com.wxm.service.impl;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.mapper.OAUserMapper;
import com.wxm.model.OAAudit;
import com.wxm.model.OAUser;
import com.wxm.service.UserService;
import org.activiti.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private OAUserMapper oaUserMapper;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private OAAuditMapper oaAuditMapper;

    @Override
    public Map getUserList(Integer offset,Integer limit,String userName){
        Map<String, Object> result = new HashMap<>();
        List<OAUser> list = oaUserMapper.list(offset,limit,userName);
        Integer count = oaUserMapper.count(userName);
        result.put("rows",list);
        result.put("total",count);
        return result;
    }

    @Override
    public OAUser getUserById(Integer id) {
        return oaUserMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<OAUser> getAllUser() {
        return oaUserMapper.getAllUser();
    }

    @Override
    public OAUser selectByName(String userName) {
        return oaUserMapper.selectByName(userName);
    }

    @Override
    public Integer create(OAUser oaUser) {
//        oaAuditMapper.insertSelective(new OAAudit(oaUser.getUserName(),String.format("新建用户 %s",oaUser.getUserName())));
        oaUser.setUserCreatetime(new Date(System.currentTimeMillis()));
        oaUserMapper.insertSelective(oaUser);
        org.activiti.engine.identity.User user = identityService.newUser(oaUser.getUserName());
        user.setFirstName(oaUser.getUserId().toString());
        user.setPassword(oaUser.getUserPwd());
        identityService.saveUser(user);
        return oaUser.getUserId();
    }

    @Override
    public Integer update(OAUser oaUser) {
        oaUser.setUserPwd(null);
        oaUser.setUserCreatetime(null);
        oaUserMapper.updateByPrimaryKeySelective(oaUser);
        org.activiti.engine.identity.User user = identityService.createUserQuery().
                userId(oaUser.getUserName()).
                userFirstName(oaUser.getUserId().toString()).singleResult();
        if(null != user) {
            user.setPassword(oaUser.getUserPwd());
            identityService.saveUser(user);
        }
        return oaUser.getUserId();
    }
    @Override
    public Integer updateStatus(OAUser oaUser) {
        oaUserMapper.updateByPrimaryKeySelective(oaUser);
        return oaUser.getUserId();
    }
    @Override
    public Integer updatePsw(OAUser oaUser) {
        oaUser.setUserCreatetime(null);
        oaUserMapper.updateByPrimaryKeySelective(oaUser);
        return oaUser.getUserId();
    }

    @Override
    public OAUser getLeader(OAUser oaUser) {
        return oaUserMapper.getLeader(oaUser);
    }


}
