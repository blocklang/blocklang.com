package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.PageWidgetAttrValue;

public interface PageWidgetAttrValueDao extends JpaRepository<PageWidgetAttrValue, Integer>{

	List<PageWidgetAttrValue> findAllByPageWidgetId(String pageWidgetId);
}
