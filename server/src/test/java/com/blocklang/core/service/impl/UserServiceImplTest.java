package com.blocklang.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.blocklang.core.constant.AvatarSizeType;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.PersisentLoginsDao;
import com.blocklang.core.dao.UserAvatarDao;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.PersisentLogins;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.core.util.LoginToken;

public class UserServiceImplTest extends AbstractServiceTest{
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PersisentLoginsDao persisentLoginsDao;
	
	@Autowired
	private UserAvatarDao userAvatarDao;
	
	@Autowired
	private UserBindDao userBindDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CacheManager cacheManager;
	
	@After
	public void tearDown() {
		// 为了避免缓存干扰其他测试用例，每次执行后都清空缓存
		this.cacheManager.getCache("users").clear();
	}
	
	@Test
	public void create_success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("login");
		userInfo.setNickname("name");
		userInfo.setCompany("company");
		userInfo.setWebsiteUrl("blog");
		userInfo.setLocation("location");
		userInfo.setEmail("email");
		userInfo.setBio("bio");
		userInfo.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=40");

		List<UserAvatar> avatarList = new ArrayList<UserAvatar>();
		
		UserAvatar small = new UserAvatar();
		small.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=40");
		small.setSizeType(AvatarSizeType.SMALL);
		avatarList.add(small);
		
		UserAvatar medium = new UserAvatar();
		medium.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=70");
		medium.setSizeType(AvatarSizeType.MEDIUM);
		avatarList.add(medium);
		
		UserAvatar large = new UserAvatar();
		large.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=150");
		large.setSizeType(AvatarSizeType.LARGE);
		avatarList.add(large);
		
		UserBind userBind = new UserBind();
		userBind.setOpenId("1");
		userBind.setSite(OauthSite.GITHUB);
		
		Integer userId = userService.create(userInfo, userBind, avatarList).getId();
		
		// 断言
		// 用户基本信息
		Optional<UserInfo> userOption = userDao.findById(userId);
		assertThat(userOption.isPresent()).isTrue();
		UserInfo user = userOption.get();
		assertThat(user.getLoginName()).isEqualTo("login");
		assertThat(user.getNickname()).isEqualTo("name");
		assertThat(user.getCompany()).isEqualTo("company");
		assertThat(user.getWebsiteUrl()).isEqualTo("blog");
		assertThat(user.getLocation()).isEqualTo("location");
		assertThat(user.getEmail()).isEqualTo("email");
		assertThat(user.getBio()).isEqualTo("bio");
		assertThat(user.getAvatarUrl()).isEqualTo("https://avatars2.githubusercontent.com/u/1?v=4&s=40");
		// 用户头像
		List<UserAvatar> avatars = userAvatarDao.findByUserId(userId);
		assertThat(avatars.size()).isEqualTo(3);
		assertThat(avatars.stream().map(avatar -> avatar.getSizeType().getKey()).collect(Collectors.toList())).allMatch(s -> s.startsWith("0"));
		assertThat(avatars.stream().map(avatar -> avatar.getAvatarUrl()).collect(Collectors.toList())).allMatch(s -> s.startsWith("https://avatars2.githubusercontent.com/u/1?v=4&s="));
		
		// hasItem(startsWith("0"))
		// 用户与社交帐号绑定关系
		Optional<UserBind> userBindOption = userBindDao.findBySiteAndOpenId(OauthSite.GITHUB, "1");
		assertThat(userBindOption.get().getUserId()).isEqualTo(userId);
	}
	
	// 确保登录名没有被修改
	@Test
	public void update_success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("login");
		userInfo.setNickname("name");
		userInfo.setCompany("company");
		userInfo.setWebsiteUrl("blog");
		userInfo.setLocation("location");
		userInfo.setEmail("email");
		userInfo.setBio("bio");
		userInfo.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=40");

		List<UserAvatar> avatarList = new ArrayList<UserAvatar>();
		
		UserAvatar small = new UserAvatar();
		small.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=40");
		small.setSizeType(AvatarSizeType.SMALL);
		avatarList.add(small);
		
		UserAvatar medium = new UserAvatar();
		medium.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=70");
		medium.setSizeType(AvatarSizeType.MEDIUM);
		avatarList.add(medium);
		
		UserAvatar large = new UserAvatar();
		large.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=150");
		large.setSizeType(AvatarSizeType.LARGE);
		avatarList.add(large);
		
		UserBind userBind = new UserBind();
		userBind.setOpenId("1");
		userBind.setSite(OauthSite.GITHUB);
		
		Integer userId = userService.create(userInfo, userBind, avatarList).getId();
		
		// 第二次更新（openId 相同）
		userInfo = new UserInfo();
		userInfo.setLoginName("login_2");
		userInfo.setNickname("name_2");
		userInfo.setCompany("company_2");
		userInfo.setWebsiteUrl("blog_2");
		userInfo.setLocation("location_2");
		userInfo.setEmail("email_2");
		userInfo.setBio("bio_2");
		userInfo.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4_2&s=40");

		avatarList = new ArrayList<UserAvatar>();
		
		small = new UserAvatar();
		small.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4_2&s=40");
		small.setSizeType(AvatarSizeType.SMALL);
		avatarList.add(small);
		
		medium = new UserAvatar();
		medium.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4_2&s=70");
		medium.setSizeType(AvatarSizeType.MEDIUM);
		avatarList.add(medium);
		
		large = new UserAvatar();
		large.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4_2&s=150");
		large.setSizeType(AvatarSizeType.LARGE);
		avatarList.add(large);
		
		Integer userId2 = userService.update(userId, userInfo, avatarList, "loginName").getId();
		
		assertThat(countRowsInTable("user_info")).isEqualTo(1);
		assertThat(userId).isEqualTo(userId2);
		
		// 断言
		// 用户基本信息
		Optional<UserInfo> userOption = userDao.findById(userId2);
		assertThat(userOption.isPresent()).isTrue();
		UserInfo user = userOption.get();
		assertThat(user.getLoginName()).isEqualTo("login"); // loginName 的值没有修改
		assertThat(user.getNickname()).isEqualTo("name_2");
		assertThat(user.getCompany()).isEqualTo("company_2");
		assertThat(user.getWebsiteUrl()).isEqualTo("blog_2");
		assertThat(user.getLocation()).isEqualTo("location_2");
		assertThat(user.getEmail()).isEqualTo("email_2");
		assertThat(user.getBio()).isEqualTo("bio_2");
		assertThat(user.getAvatarUrl()).isEqualTo("https://avatars2.githubusercontent.com/u/1?v=4_2&s=40");
		// 用户头像
		List<UserAvatar> avatars = userAvatarDao.findByUserId(userId2);
		assertThat(avatars.stream().map(avatar -> avatar.getAvatarUrl()).collect(Collectors.toList()))
			.allMatch(s -> s.startsWith("https://avatars2.githubusercontent.com/u/1?v=4_2&s="));
	}
	
	@Test
	public void find_by_login_token_no_data() {
		assertThat(userService.findByLoginToken("not-exist-token")).isEmpty();
	}
	
	@Test
	public void find_by_login_token_success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("login");
		userInfo.setNickname("name");
		userInfo.setCompany("company");
		userInfo.setWebsiteUrl("blog");
		userInfo.setLocation("location");
		userInfo.setEmail("email");
		userInfo.setBio("bio");
		userInfo.setCreateTime(LocalDateTime.now());
		userInfo.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=40");
		
		userDao.save(userInfo).getId();
		
		LocalDateTime firstUpdateTime = LocalDateTime.now().minusMinutes(1);
		
		PersisentLogins login = new PersisentLogins();
		login.setLoginName("login");
		login.setToken("x-token");
		login.setLastUsedTime(firstUpdateTime);
		
		Integer loginId = persisentLoginsDao.save(login).getId();
		
		assertThat(userService.findByLoginToken("x-token")).isPresent();
		assertThat(persisentLoginsDao.findById(loginId).get().getLastUsedTime()).isAfter(firstUpdateTime);
	}
	
	@Test
	public void generate_login_token_one_time() {
		String token = userService.generateLoginToken(OauthSite.QQ,"login");
		
		LoginToken loginToken = new LoginToken();
		loginToken.decode(token);
		Optional<PersisentLogins> plOPtion = persisentLoginsDao.findByToken(loginToken.getToken());
		assertThat(plOPtion).isPresent();
		assertThat(plOPtion.get()).hasNoNullFieldsOrProperties();
	}
	
	@Test
	public void generate_login_token_twice() {
		String token1 = userService.generateLoginToken(OauthSite.QQ, "login");
		String token2 = userService.generateLoginToken(OauthSite.QQ, "login");
		
		assertThat(token1).isNotEqualTo(token2);
		
		LoginToken loginToken = new LoginToken();
		loginToken.decode(token2);
		
		Optional<PersisentLogins> plOPtion = persisentLoginsDao.findByToken(loginToken.getToken());
		assertThat(plOPtion).isPresent();
		assertThat(plOPtion.get()).hasNoNullFieldsOrProperties();
	}
	
	@Test
	public void find_by_login_name_cacheable() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("login");
		userInfo.setNickname("name");
		userInfo.setCompany("company");
		userInfo.setWebsiteUrl("blog");
		userInfo.setLocation("location");
		userInfo.setEmail("email");
		userInfo.setBio("bio");
		userInfo.setAvatarUrl("https://avatars2.githubusercontent.com/u/1?v=4&s=40");
		userInfo.setCreateTime(LocalDateTime.now());
		userDao.save(userInfo);
		
		Cache cachedUsers = this.cacheManager.getCache("users");
		Optional<UserInfo> savedUserOption = userService.findByLoginName("login");
		assertThat(cachedUsers.get("login").get()).isEqualTo(savedUserOption.get());
	}
}
