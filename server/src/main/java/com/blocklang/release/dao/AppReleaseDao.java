package com.blocklang.release.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.AppRelease;

public interface AppReleaseDao extends JpaRepository<AppRelease, Integer> {

	Optional<AppRelease> findFirstByAppIdOrderByIdDesc(Integer appId);

	Optional<AppRelease> findByAppIdAndVersion(Integer appId, String version);

	/**
	 * 
	 * @param appId
	 * @return app 发布列表，按照发布时间倒排，因为 id 和 appId 的顺序一致，所以使用 id
	 */
	List<AppRelease> findByAppIdOrderByIdDesc(Integer appId);

}
