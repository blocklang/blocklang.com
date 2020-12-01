package com.blocklang.marketplace.service;

import java.util.Optional;

import com.blocklang.marketplace.model.ApiRepoVersion;

/**
 * Api 仓库版本管理数据服务接口。
 * 
 * <p> 一个 Api 仓库中有多个稳定版本和一个 master 版本。
 * 使用 git tag 标注稳定版本，在 master 分支中存最新版本。
 * 
 * @author Zhengwei Jin
 *
 */
public interface ApiRepoVersionService {

	/**
	 * 根据 Api 仓库的版本标识获取 Api 仓库的版本信息
	 * 
	 * @param apiRepoVersionId 根据 Api 仓库的版本标识
	 * @return Api 仓库的版本信息
	 */
	Optional<ApiRepoVersion> findById(Integer apiRepoVersionId);

	/**
	 * 获取 Api 仓库中最新稳定版版本信息
	 * 
	 * @param apiRepoId Api 仓库标识
	 * @return 最新稳定版本信息
	 */
	Optional<ApiRepoVersion> findLatestStableVersion(Integer apiRepoId);

	/**
	 * 获取 Api 仓库的 master/main 分支信息。
	 * 
	 * @param apiRepoId Api 仓库标识
	 * @return master/main 分支信息
	 */
	Optional<ApiRepoVersion> findMasterVersion(Integer apiRepoId);
}
