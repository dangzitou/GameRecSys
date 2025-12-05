package com.sparrowrecsys.online.mapper;

import com.sparrowrecsys.online.model.UserAccount;
import org.apache.ibatis.annotations.Param;

/**
 * 用户账户 Mapper 接口
 */
public interface UserAccountMapper {
    
    /**
     * 根据用户名查询用户
     */
    UserAccount selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    UserAccount selectByEmail(@Param("email") String email);
    
    /**
     * 根据ID查询用户
     */
    UserAccount selectById(@Param("id") Integer id);
    
    /**
     * 插入新用户
     */
    int insert(UserAccount userAccount);
    
    /**
     * 更新用户信息
     */
    int update(UserAccount userAccount);
    
    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Integer id);
}

