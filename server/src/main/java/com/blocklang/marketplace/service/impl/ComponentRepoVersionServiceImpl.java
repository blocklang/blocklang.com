package com.blocklang.marketplace.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoVersionService;

import de.skuzzle.semantic.Version;

@Service
public class ComponentRepoVersionServiceImpl implements ComponentRepoVersionService{

	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	
	@Override
	public Optional<ComponentRepoVersion> findById(Integer componentRepoVersionId) {
		return componentRepoVersionDao.findById(componentRepoVersionId);
	}

	@Override
	public List<ComponentRepoVersion> findAllByComponentRepoId(Integer componentRepoId) {
		return componentRepoVersionDao.findAllByComponentRepoId(componentRepoId);
	}

	@Override
	public Optional<ComponentRepoVersion> findLatestVersion(Integer componentRepoId) {
		List<ComponentRepoVersion> allVersions = componentRepoVersionDao.findAllByComponentRepoId(componentRepoId);
		if(allVersions.isEmpty()) {
			return Optional.empty();
		}
		// 要先过滤掉 master 版本
		List<ComponentRepoVersion> filtered = allVersions.stream().filter(apiRepoVersion -> !apiRepoVersion.getVersion().equals("master")).collect(Collectors.toList());
		if(filtered.isEmpty()) {
			return Optional.empty();
		}
		// 最新版本在最前面
		filtered.sort(new Comparator<ComponentRepoVersion>() {
			@Override
			public int compare(ComponentRepoVersion version1, ComponentRepoVersion version2) {
				return Version.compare(Version.parseVersion(version2.getVersion()), Version.parseVersion(version1.getVersion()));
			}
		});
		return Optional.of(filtered.get(0));
	}

	@Override
	public Optional<ComponentRepoVersion> findByComponentIdAndVersion(Integer componentRepoId, String version) {
		return componentRepoVersionDao.findByComponentRepoIdAndVersion(componentRepoId, version);
	}

}
