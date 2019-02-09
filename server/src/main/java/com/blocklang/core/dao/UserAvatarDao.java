package com.blocklang.core.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.core.model.UserAvatar;

public interface UserAvatarDao extends JpaRepository<UserAvatar, Integer>{

	List<UserAvatar> findByUserId(Integer userId);

}
