package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.PageDataItem;

public interface PageDataDao extends JpaRepository<PageDataItem, Integer> {

	List<PageDataItem> findAllByPageId(Integer pageId);

}
