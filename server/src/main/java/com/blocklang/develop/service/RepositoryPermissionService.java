package com.blocklang.develop.service;

import java.security.Principal;
import java.util.Optional;

import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.model.Repository;

/**
 * 仓库类型：公开和私有；权限类型：只读（READ）、可写（WRITE）和管理（ADMIN）。
 * 
 * <p>匿名用户
 * <ol>
 * <li>匿名用户可以 READ 所有公开仓库</li>
 * <li>匿名用户不能 READ 所有私有仓库</li>
 * 
 * <li>匿名用户不能 WRITE 所有公开仓库</li>
 * <li>匿名用户不能 WRITE 所有私有仓库</li>
 * 
 * <li>匿名用户不能 ADMIN 所有公开仓库</li>
 * <li>匿名用户不能 ADMIN 所有私有仓库</li>
 * </ol>
 * <p>
 * 
 * <p>登录用户
 * <ol>
 * <li>登录用户可以 READ 所有公开仓库</li>
 * <li>登录用户不能 READ 无任何权限的私有仓库</li>
 * <li>登录用户可以 READ 有 READ 权限的私有仓库</li>
 * <li>登录用户可以 READ 有 WRITE 权限的私有仓库</li>
 * <li>登录用户可以 READ 有 ADMIN 权限的私有仓库</li>
 * 
 * <li>登录用户不能 WRITE 无任何权限的公开仓库</li>
 * <li>登录用户不能 WRITE 有 READ 权限的公开仓库</li>
 * <li>登录用户可以 WRITE 有 WRITE 权限的公开仓库</li>
 * <li>登录用户可以 WRITE 有 ADMIN 权限的公开仓库</li>
 * <li>登录用户不能 WRITE 无任何权限的私有仓库</li>
 * <li>登录用户不能 WRITE 有 READ 权限的私有仓库</li>
 * <li>登录用户可以 WRITE 有 WRITE 权限的私有仓库</li>
 * <li>登录用户可以 WRITE 有 ADMIN 权限的私有仓库</li>
 * 
 * <li>登录用户不能 ADMIN 无任何权限的公开仓库</li>
 * <li>登录用户不能 ADMIN 有 READ 权限的公开仓库</li>
 * <li>登录用户不能 ADMIN 有 WRITE 权限的公开仓库</li>
 * <li>登录用户可以 ADMIN 有 ADMIN 权限的公开仓库</li>
 * <li>登录用户不能 ADMIN 无任何权限的私有仓库</li>
 * <li>登录用户不能 ADMIN 有 READ 权限的私有仓库</li>
 * <li>登录用户不能 ADMIN 有 WRITE 权限的私有仓库</li>
 * <li>登录用户可以 ADMIN 有 ADMIN 权限的私有仓库</li>
 * </ol>
 * </p>
 * 
 * @author jinzw
 *
 */
public interface RepositoryPermissionService {

	/**
	 * 校验登录用户对仓库是否有读取权限。
	 * 
	 * @param loginUser 登录用户信息
	 * @param repository 仓库基本信息
	 * @return 如果登录用户可以访问仓库，则返回权限信息，否则返回  <code>Optional.empty()</code>。
	 * <p>注意，不要使用返回的权限信息，此处本应返回 <code>Boolean</code> 类型，
	 * 之所以返回 <code>Optional&lt;AccessLevel&gt;</code>，
	 * 是为了使用 {@link Optional#orElseThrow(java.util.function.Supplier)} </p>
	 */
	Optional<AccessLevel> canRead(Principal loginUser, Repository repository);
	
	/**
	 * 校验登录用户对仓库是否有写入权限。
	 * 
	 * @param loginUser 登录用户信息
	 * @param repository 仓库基本信息
	 * @return 如果登录用户可以写入仓库，则返回权限信息，否则返回  <code>Optional.empty()</code>
	 * <p>注意，不要使用返回的权限信息，此处本应返回 <code>Boolean</code> 类型，
	 * 之所以返回 <code>Optional&lt;AccessLevel&gt;</code>，
	 * 是为了使用 {@link Optional#orElseThrow(java.util.function.Supplier)} </p>
	 */
	Optional<AccessLevel> canWrite(Principal loginUser, Repository repository);

	/**
	 * 校验登录用户对仓库是否有管理权限。
	 * 
	 * @param loginUser 登录用户信息
	 * @param repository 仓库基本信息
	 * @return 如果登录用户可以管理仓库，则返回权限信息，否则返回  <code>Optional.empty()</code>
	 * <p>注意，不要使用返回的权限信息，此处本应返回 <code>Boolean</code> 类型，
	 * 之所以返回 <code>Optional&lt;AccessLevel&gt;</code>，
	 * 是为了使用 {@link Optional#orElseThrow(java.util.function.Supplier)} </p>
	 */
	Optional<AccessLevel> canAdmin(Principal loginUser, Repository repository);
	
	/**
	 * 如果要获取用户对仓库拥有的最高权限，则应使用此方法，
	 * 因为权限是分等级的，canRead 等方法是用于校验用户对仓库是否拥有某项权限，返回的权限信息不一定是实际拥有的最高权限。
	 * 
	 * @param loginUser 登录用户信息
	 * @param repository 仓库基本信息
	 * @return 权限信息
	 */
	AccessLevel findTopestPermission(Principal loginUser, Repository repository);
}
