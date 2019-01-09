package com.blocklang.release.api;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.release.data.InstallerInfo;

@RestController
@RequestMapping("/installers")
public class InstallerApi {

	@GetMapping
	public InstallerInfo getRegister(HttpServletResponse resp) {
		resp.setStatus(HttpServletResponse.SC_CREATED);
		
		return new InstallerInfo();
	}
	
	@PostMapping
	public InstallerInfo newRegister(HttpServletResponse resp) {
		resp.setStatus(HttpServletResponse.SC_CREATED);
		
		return new InstallerInfo();
	}
}
