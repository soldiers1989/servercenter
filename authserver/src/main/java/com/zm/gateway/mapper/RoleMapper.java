package com.zm.gateway.mapper;

import java.util.List;

import com.zm.gateway.model.Role;

/**
 * @author
 * @version 1.0 2016/10/11
 * @description
 */
public interface RoleMapper {

    /**
     * 根据用户ID获取用户角色
     * @param userId
     * @return
     */
    public List<Role> getUserRole(String userId);
}
