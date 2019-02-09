package com.blocklang.core.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.model.UserBind;

public interface UserBindDao extends JpaRepository<UserBind, Integer>{

	Optional<UserBind> findBySiteAndOpenId(OauthSite github, String openId);

}
