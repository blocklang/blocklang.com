package com.blocklang.release.service;

import java.util.Optional;

public interface AppReleaseRelationService {

	/**
	 * 找到 appReleaseId 指定的 APP 的唯一依赖的 APP 标识
	 * 
	 * @param appReleaseId APP 发行版标识
	 * @return 依赖的 APP 发行版表示
	 */
	Optional<Integer> findSingle(int appReleaseId);

}
