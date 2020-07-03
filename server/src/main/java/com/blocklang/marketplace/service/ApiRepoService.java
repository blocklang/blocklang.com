package com.blocklang.marketplace.service;

import java.util.Optional;

import com.blocklang.marketplace.model.ApiRepo;

/**
 * Api 仓库的数据访问接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface ApiRepoService {

	/**
	 * 根据 Api 仓库的主键获取 Api 仓库信息
	 * 
	 * @param apiRepoId Api 仓库主键
	 * @return Api 仓库信息
	 */
	Optional<ApiRepo> findById(Integer apiRepoId);

	/**
	 * 根据 Api 仓库的 git url 和创建用户标识查询唯一的 Api 仓库信息
	 * @param apiGitUrl api 仓库的 git url
	 * @param userId 发布此 api 仓库的用户标识
	 * @return Api 仓库信息
	 */
	Optional<ApiRepo> findByGitUrlAndCreateUserId(String apiGitUrl, Integer userId);

}
