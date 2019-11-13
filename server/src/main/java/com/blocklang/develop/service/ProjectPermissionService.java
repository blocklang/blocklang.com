package com.blocklang.develop.service;

import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.model.Project;
import java.security.Principal;
import java.util.Optional;

/**
 * 项目类型：公开和私有；权限类型：只读（READ）、可写（WRITE）和管理（ADMIN）。
 * 
 * <p>匿名用户
 * <ol>
 * <li>匿名用户可以 READ 所有公开项目</li>
 * <li>匿名用户不能 READ 所有私有项目</li>
 * 
 * <li>匿名用户不能 WRITE 所有公开项目</li>
 * <li>匿名用户不能 WRITE 所有私有项目</li>
 * 
 * <li>匿名用户不能 ADMIN 所有公开项目</li>
 * <li>匿名用户不能 ADMIN 所有私有项目</li>
 * </ol>
 * <p>
 * 
 * <p>登录用户
 * <ol>
 * <li>登录用户可以 READ 所有公开项目</li>
 * <li>登录用户不能 READ 无任何权限的私有项目</li>
 * <li>登录用户可以 READ 有 READ 权限的私有项目</li>
 * <li>登录用户可以 READ 有 WRITE 权限的私有项目</li>
 * <li>登录用户可以 READ 有 ADMIN 权限的私有项目</li>
 * 
 * <li>登录用户不能 WRITE 无任何权限的公开项目</li>
 * <li>登录用户不能 WRITE 有 READ 权限的公开项目</li>
 * <li>登录用户可以 WRITE 有 WRITE 权限的公开项目</li>
 * <li>登录用户可以 WRITE 有 ADMIN 权限的公开项目</li>
 * <li>登录用户不能 WRITE 无任何权限的私有项目</li>
 * <li>登录用户不能 WRITE 有 READ 权限的私有项目</li>
 * <li>登录用户可以 WRITE 有 WRITE 权限的私有项目</li>
 * <li>登录用户可以 WRITE 有 ADMIN 权限的私有项目</li>
 * 
 * <li>登录用户不能 ADMIN 无任何权限的公开项目</li>
 * <li>登录用户不能 ADMIN 有 READ 权限的公开项目</li>
 * <li>登录用户不能 ADMIN 有 WRITE 权限的公开项目</li>
 * <li>登录用户可以 ADMIN 有 ADMIN 权限的公开项目</li>
 * <li>登录用户不能 ADMIN 无任何权限的私有项目</li>
 * <li>登录用户不能 ADMIN 有 READ 权限的私有项目</li>
 * <li>登录用户不能 ADMIN 有 WRITE 权限的私有项目</li>
 * <li>登录用户可以 ADMIN 有 ADMIN 权限的私有项目</li>
 * </ol>
 * </p>
 * 
 * @author jinzw
 *
 */
public interface ProjectPermissionService {

	/**
	 * 校验登录用户对项目是否有读取权限。
	 * 
	 * @param loginUser 登录用户信息
	 * @param project 项目基本信息
	 * @return 如果登录用户可以访问项目，则返回权限信息，否则返回  <code>Optional.empty()</code>
	 */
	Optional<AccessLevel> canRead(Principal loginUser, Project project);
	
	/**
	 * 校验登录用户对项目是否有写入权限。
	 * 
	 * @param loginUser 登录用户信息
	 * @param project 项目基本信息
	 * @return 如果登录用户可以写入项目，则返回权限信息，否则返回  <code>Optional.empty()</code>
	 */
	Optional<AccessLevel> canWrite(Principal loginUser, Project project);

	/**
	 * 校验登录用户对项目是否有管理权限。
	 * 
	 * @param userName 登录用户的登录名，对应 {@link UserInfo#getLoginName()}}
	 * @param loginUser 登录用户信息
	 * @return 如果登录用户可以管理项目，则返回权限信息，否则返回  <code>Optional.empty()</code>
	 */
	Optional<AccessLevel> canAdmin(Principal loginUser, Project project);
}
