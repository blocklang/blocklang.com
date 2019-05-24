package com.blocklang.marketplace.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.marketplace.model.ComponentRepoRegistry;
import com.blocklang.marketplace.service.ComponentRepoRegistryService;

@RestController
public class ComponentRepoController {
	
	@Autowired
	private ComponentRepoRegistryService componentRepoRegistryService;

	@GetMapping("/component-repos")
	public ResponseEntity<Page<ComponentRepoRegistry>> listComponentRepos(
			@RequestParam(value="q", required = false)String query, 
			@RequestParam(required = false) String page) {
		
		Integer iPage = null;
		if(StringUtils.isBlank(page)){
			iPage = 0;
		}else {
			try {
				iPage = Integer.valueOf(page);
			}catch (NumberFormatException e) {
				throw new ResourceNotFoundException();
			}
		}
		
		if(iPage < 0) {
			throw new ResourceNotFoundException();
		}
		
		// 默认一页显示 60 项组件仓库
		Pageable pageable = PageRequest.of(iPage, 60, Sort.by(Direction.DESC, "lastPublishTime"));
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel(query, pageable);
		
		if(iPage > result.getTotalPages()) {
			throw new ResourceNotFoundException();
		}
		
		return ResponseEntity.ok(result);
	}
	
}
