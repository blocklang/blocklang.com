package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.PageFunctionNode;

public interface PageFunctionNodeDao extends JpaRepository<PageFunctionNode, Integer> {

	List<PageFunctionNode> findAllByPageId(Integer pageId);

}
