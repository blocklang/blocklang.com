package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiWidget;

public interface ApiWidgetDao extends JpaRepository<ApiWidget, Integer> {

	List<ApiWidget> findAllByApiRepoVersionId(Integer apiRepoVersionId);

	Optional<ApiWidget> findByApiRepoVersionIdAndNameIgnoreCase(Integer apiRepoVersionId, String name);
}
