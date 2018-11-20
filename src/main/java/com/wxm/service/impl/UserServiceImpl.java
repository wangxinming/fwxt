package com.wxm.service.impl;

import com.wxm.mapper.OAAuditMapper;
import com.wxm.mapper.OAGroupMapper;
import com.wxm.mapper.OAUserMapper;
import com.wxm.model.OAAudit;
import com.wxm.model.OAGroup;
import com.wxm.model.OAUser;
import com.wxm.service.UserService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private OAUserMapper oaUserMapper;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private OAAuditMapper oaAuditMapper;
    @Autowired
    private OAGroupMapper oaGroupMapper;

    @Override
    public Map getUserList(Integer offset,Integer limit,String userName){
        Map<String, Object> result = new HashMap<>();
        List<OAGroup> oaGroupList = oaGroupMapper.total();
        Map<Integer,String> map = new LinkedHashMap<>();
        for(OAGroup oaGroup : oaGroupList){
            map.put(oaGroup.getGroupId(),oaGroup.getGroupName());
        }
        List<OAUser> list = oaUserMapper.list(offset,limit,userName);
        for(OAUser oaUser:list ){
            if(null != oaUser.getGroupId() && oaUser.getGroupId() > 0 && map.containsKey(oaUser.getGroupId())){
                oaUser.setGroupName(map.get(oaUser.getGroupId()));
            }
        }
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
    public List<OAUser> getPMUser() {
        return oaUserMapper.listUserPM();
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
        User userOa = identityService.createUserQuery().userId(oaUser.getUserName()).singleResult();
        if(null == userOa) {
            org.activiti.engine.identity.User user = identityService.newUser(oaUser.getUserName());
            user.setFirstName(oaUser.getUserId().toString());
            user.setPassword(oaUser.getUserPwd());
            identityService.saveUser(user);
        }
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

    @Override
    public List<OAUser> listUserLeader(String company, String position) {
        return oaUserMapper.listUserLeader(company,position);
    }

    @Override
    public List<OAUser> groupByPosition() {
        return oaUserMapper.groupByPosition();
    }

    @Override
    public List<OAUser> listUserByCompany(Integer companyId,String position) {
        return oaUserMapper.listUserByCompany(companyId,position);
    }


}
