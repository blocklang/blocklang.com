package com.blocklang.core.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.core.model.UserInfo;

public interface UserDao extends JpaRepository<UserInfo, Integer>{

}
