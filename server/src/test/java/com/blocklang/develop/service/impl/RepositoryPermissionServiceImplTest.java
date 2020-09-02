package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.dao.RepositoryAuthorizationDao;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryAuthorization;
import com.blocklang.develop.service.RepositoryPermissionService;

public class RepositoryPermissionServiceImplTest extends AbstractServiceTest{

	@Autowired
	private RepositoryPermissionService repositoryPermissionService;
	@MockBean
	private RepositoryAuthorizationDao repositoryAuthorizationDao;
	@MockBean
	private UserService userService;
	
	private Principal loginUser;
	
	@BeforeEach
	public void setUp() {
		loginUser = new Principal() {
			@Override
			public String getName() {
				return "jack";
			}
		};
	}
	
	@Test
	public void ensure_can_read_anonymous_can_read_all_public_project() {
		Repository project = new Repository();
		project.setIsPublic(true);
		assertThat(repositoryPermissionService.canRead(null, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_anonymous_can_not_read_all_private_project() {
		Repository project = new Repository();
		project.setIsPublic(false);
		assertThat(repositoryPermissionService.canRead(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_all_public_project() {
		Repository project = new Repository();
		project.setIsPublic(true);
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_not_read_private_project_when_user_not_exist() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_not_read_private_project_not_config_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_not_read_private_project_that_has_forbidden_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_write_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_admin_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_forbidden_and_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth1 = new RepositoryAuthorization();
		auth1.setAccessLevel(AccessLevel.FORBIDDEN);
		
		RepositoryAuthorization auth2 = new RepositoryAuthorization();
		auth2.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Arrays.asList(auth1, auth2));
		
		assertThat(repositoryPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	
	@Test
	public void ensure_can_write_anonymous_can_not_write_all_public_project() {
		Repository project = new Repository();
		project.setIsPublic(true);
		assertThat(repositoryPermissionService.canWrite(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_anonymous_can_not_write_all_private_project() {
		Repository project = new Repository();
		project.setIsPublic(false);
		assertThat(repositoryPermissionService.canWrite(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_when_user_not_exist() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_that_not_config_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_that_has_forbidden_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_that_has_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_public_project_that_has_write_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_public_project_that_has_admin_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isPresent();
	}

	public void ensure_can_write_login_user_can_not_write_private_project_when_user_not_exist() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_private_project_that_not_config_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_private_project_that_has_forbidden_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_private_project_that_has_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_private_project_that_has_write_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_private_project_that_has_admin_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canWrite(loginUser, project)).isPresent();
	}

	
	
	
	
	
	public void ensure_can_admin_anonymous_can_not_admin_all_public_project() {
		Repository project = new Repository();
		project.setIsPublic(true);
		assertThat(repositoryPermissionService.canAdmin(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_anonymous_can_not_admin_all_private_project() {
		Repository project = new Repository();
		project.setIsPublic(false);
		assertThat(repositoryPermissionService.canAdmin(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_when_user_not_exist() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_that_not_config_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_that_has_forbidden_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_that_has_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_public_project_that_has_write_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_public_project_that_has_admin_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isPresent();
	}

	public void ensure_can_admin_login_user_can_not_admin_private_project_when_user_not_exist() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_private_project_that_not_config_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_private_project_that_has_forbidden_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_private_project_that_has_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_private_project_that_has_write_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_private_project_that_has_admin_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.canAdmin(loginUser, project)).isPresent();
	}
	
	@Test
	public void find_topest_permission_anonymous_can_read_public_project() {
		Repository project = new Repository();
		project.setIsPublic(true);
		assertThat(repositoryPermissionService.findTopestPermission(null, project)).isEqualTo(AccessLevel.READ);
	}
	
	@Test
	public void find_topest_permission_anonymous_can_not_read_private_project() {
		Repository project = new Repository();
		project.setIsPublic(false);
		assertThat(repositoryPermissionService.findTopestPermission(null, project)).isEqualTo(AccessLevel.FORBIDDEN);
	}

	@Test
	public void find_topest_permission_login_can_read_public_project_that_config_no_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(2);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		assertThat(repositoryPermissionService.findTopestPermission(loginUser, project)).isEqualTo(AccessLevel.READ);
	}
	
	@Test
	public void find_topest_permission_login_can_read_public_project_that_has_read_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(2);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.findTopestPermission(loginUser, project)).isEqualTo(AccessLevel.READ);
	}
	
	@Test
	public void find_topest_permission_login_can_read_public_project_that_has_write_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(2);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.findTopestPermission(loginUser, project)).isEqualTo(AccessLevel.WRITE);
	}
	
	@Test
	public void find_topest_permission_login_can_read_public_project_that_has_admin_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(2);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(repositoryPermissionService.findTopestPermission(loginUser, project)).isEqualTo(AccessLevel.ADMIN);
	}
	
	@Test
	public void find_topest_permission_login_can_read_public_project_that_has_two_permissions() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(2);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		RepositoryAuthorization auth1 = new RepositoryAuthorization();
		auth1.setAccessLevel(AccessLevel.READ);
		RepositoryAuthorization auth2 = new RepositoryAuthorization();
		auth2.setAccessLevel(AccessLevel.ADMIN);
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Arrays.asList(auth1, auth2));
		assertThat(repositoryPermissionService.findTopestPermission(loginUser, project)).isEqualTo(AccessLevel.ADMIN);
	}
	
	@Test
	public void find_topest_permission_login_can_not_read_private_project_that_config_no_permission() {
		Repository project = new Repository();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(2);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryAuthorizationDao.findAllByUserIdAndRepositoryId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		assertThat(repositoryPermissionService.findTopestPermission(loginUser, project)).isEqualTo(AccessLevel.FORBIDDEN);
	}
}
