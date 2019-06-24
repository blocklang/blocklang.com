package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiChangelog;

public interface ApiChangelogDao extends JpaRepository<ApiChangelog, Integer> {

	List<ApiChangelog> findAllByApiRepoId(Integer apiRepoId);

}
