package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.PageWidget;

public interface PageWidgetDao extends JpaRepository<PageWidget, Integer>{

	List<PageWidget> findAllByPageIdOrderBySeq(Integer pageId);

}
