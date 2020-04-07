package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.PageFunctionNodePort;

public interface PageFunctionNodePortDao extends JpaRepository<PageFunctionNodePort, Integer> {

	List<PageFunctionNodePort> findAllByPageId(Integer pageId);

}
