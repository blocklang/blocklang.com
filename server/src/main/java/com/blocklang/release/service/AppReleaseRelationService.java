package com.blocklang.release.service;

import java.util.Optional;

public interface AppReleaseRelationService {

	/**
	 * 找到 appReleaseId 指定的 APP 的唯一依赖的 APP 标识。
	 * 
	 * 如果查出多条记录，则返回空。
	 * 
	 * @param appReleaseId APP 发行版标识
	 * @return 依赖的 APP 发行版标识
	 */
	Optional<Integer> findSingle(int appReleaseId);

}
