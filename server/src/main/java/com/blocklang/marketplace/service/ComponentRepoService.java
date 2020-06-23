package com.blocklang.marketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ComponentRepo;

public interface ComponentRepoService {

	/**
	 * 只查找已发布的组件库，如果组件库已登记，但是没有发布，则不返回。
	 * 
	 * 
	 * @param query
	 * @param page
	 * @return
	 */
	Page<ComponentRepoInfo> findAllByGitRepoNameAndExcludeStd(String query, Pageable page);

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
