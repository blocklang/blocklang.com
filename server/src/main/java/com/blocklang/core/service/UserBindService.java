package com.blocklang.core.service;

import java.util.Optional;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.model.UserBind;

public interface UserBindService {

	Optional<UserBind> findBySiteAndOpenId(OauthSite site, String openId);

}
