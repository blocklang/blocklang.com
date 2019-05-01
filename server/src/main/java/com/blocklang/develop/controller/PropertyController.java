package com.blocklang.develop.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.develop.constant.AppType;

@RestController
public class PropertyController {

	@GetMapping("/properties/{name}")
	public ResponseEntity<List<Map<String, String>>> get(@PathVariable String name) {
		
		List<Map<String, String>> result = new ArrayList<>();
		
		if("app-type".equals(name)) {
			result = Arrays.stream(AppType.values())
				.filter(item -> item.getKey().equals("01")) // 0.1.1 版本只支持 web
				.map(item -> {
					Map<String, String> map = new HashMap<String, String>();
					map.put("key", item.getKey());
					map.put("value", item.getLabel());
					map.put("icon", item.getIcon());
					return map;
				}).collect(Collectors.toList());
		}
		
		return ResponseEntity.ok(result);
		
	}
	
}
