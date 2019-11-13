package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.dao.ProjectAuthorizationDao;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectPermissionService;

public class ProjectPermissionServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectPermissionService projectPermissionService;
	@MockBean
	private ProjectAuthorizationDao projectAuthorizationDao;
	@MockBean
	private UserService userService;
	
	private Principal loginUser;
	
	@Before
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
		Project project = new Project();
		project.setIsPublic(true);
		assertThat(projectPermissionService.canRead(null, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_anonymous_can_not_read_all_private_project() {
		Project project = new Project();
		project.setIsPublic(false);
		assertThat(projectPermissionService.canRead(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_all_public_project() {
		Project project = new Project();
		project.setIsPublic(true);
		assertThat(projectPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_not_read_private_project_when_user_not_exist() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(projectPermissionService.canRead(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_not_read_private_project_not_config_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(projectPermissionService.canRead(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_not_read_private_project_that_has_forbidden_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canRead(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_write_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_admin_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_read_login_user_can_read_private_project_that_has_forbidden_and_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth1 = new ProjectAuthorization();
		auth1.setAccessLevel(AccessLevel.FORBIDDEN);
		
		ProjectAuthorization auth2 = new ProjectAuthorization();
		auth2.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Arrays.asList(auth1, auth2));
		
		assertThat(projectPermissionService.canRead(loginUser, project)).isPresent();
	}
	
	
	@Test
	public void ensure_can_write_anonymous_can_not_write_all_public_project() {
		Project project = new Project();
		project.setIsPublic(true);
		assertThat(projectPermissionService.canWrite(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_anonymous_can_not_write_all_private_project() {
		Project project = new Project();
		project.setIsPublic(false);
		assertThat(projectPermissionService.canWrite(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_when_user_not_exist() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_that_not_config_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_that_has_forbidden_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_public_project_that_has_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_public_project_that_has_write_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_public_project_that_has_admin_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isPresent();
	}

	public void ensure_can_write_login_user_can_not_write_private_project_when_user_not_exist() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_private_project_that_not_config_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_private_project_that_has_forbidden_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_not_write_private_project_that_has_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_private_project_that_has_write_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isPresent();
	}
	
	@Test
	public void ensure_can_write_login_user_can_write_private_project_that_has_admin_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canWrite(loginUser, project)).isPresent();
	}

	
	
	
	
	
	public void ensure_can_admin_anonymous_can_not_admin_all_public_project() {
		Project project = new Project();
		project.setIsPublic(true);
		assertThat(projectPermissionService.canAdmin(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_anonymous_can_not_admin_all_private_project() {
		Project project = new Project();
		project.setIsPublic(false);
		assertThat(projectPermissionService.canAdmin(null, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_when_user_not_exist() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_that_not_config_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_that_has_forbidden_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_public_project_that_has_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_public_project_that_has_write_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_public_project_that_has_admin_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isPresent();
	}

	public void ensure_can_admin_login_user_can_not_admin_private_project_when_user_not_exist() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_private_project_that_not_config_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_private_project_that_has_forbidden_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.FORBIDDEN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_not_admin_private_project_that_has_read_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_private_project_that_has_write_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isEmpty();
	}
	
	@Test
	public void ensure_can_admin_login_user_can_admin_private_project_that_has_admin_permission() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setAccessLevel(AccessLevel.ADMIN);
		when(projectAuthorizationDao.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		assertThat(projectPermissionService.canAdmin(loginUser, project)).isPresent();
	}

}
