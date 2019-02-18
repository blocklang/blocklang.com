package com.blocklang.core.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.core.model.UserInfo;

public interface UserDao extends JpaRepository<UserInfo, Integer>{

	Optional<UserInfo> findByLoginName(String loginName);

}
