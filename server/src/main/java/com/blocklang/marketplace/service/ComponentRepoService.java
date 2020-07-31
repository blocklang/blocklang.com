package com.blocklang.marketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ComponentRepo;

public interface ComponentRepoService {

	/**
	 * 根据 git url 过滤组件库。
	 * 
	 * 本方法会查出标准库，并且会将标准库的 std 设置为 true。
	 * 
	 * 
	 * @param queryGitRepoName git url 的过滤条件
	 * @param page 分页信息
	 * @return 组件库列表
	 */
	Page<ComponentRepoInfo> findAllByGitRepoName(String queryGitRepoName, Pageable page);

	/**
	 * 返回用户发布的组件库
	 * 
	 * <p>
	 * 注意：此方法要返回标准库。
	 * </p>
	 * 
	 * 
	 * @param userId
	 * @return 用户发布的组件库，根据仓库的名称正序排列。
	 */
	List<ComponentRepoInfo> findUserComponentRepos(Integer userId);

	boolean existsByCreateUserIdAndGitRepoUrl(Integer userId, String gitRepoUrl);

	Optional<ComponentRepo> findById(Integer componentRepoId);

}
