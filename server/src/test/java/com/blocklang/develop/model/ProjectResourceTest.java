package com.blocklang.develop.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.blocklang.core.constant.Constant;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;

public class ProjectResourceTest {

	@Test
	public void is_main_program_success() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setKey(ProjectResource.MAIN_KEY);
		
		assertThat(resource.isMain()).isTrue();
	}
	
	@Test
	public void is_main_program_at_root_not_main_key() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setKey("a");
		
		assertThat(resource.isMain()).isFalse();
	}
	
	@Test
	public void is_main_program_not_at_root_not_main_key() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(1);
		resource.setKey(ProjectResource.MAIN_KEY);
		
		assertThat(resource.isMain()).isFalse();
	}
	
	@Test
	public void is_templet() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM_TEMPLET);
		
		assertThat(resource.isTemplet()).isTrue();
	}
	
	@Test
	public void is_function() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FUNCTION);
		
		assertThat(resource.isFunction()).isTrue();
	}
	
	@Test
	public void is_program() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		
		assertThat(resource.isProgram()).isTrue();
	}
	
	@Test
	public void is_file() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FILE);
		
		assertThat(resource.isFile()).isTrue();
	}
	
	@Test
	public void is_readme() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FILE);
		resource.setKey(ProjectResource.README_KEY);
		
		assertThat(resource.isReadme()).isTrue();
	}
	
	@Test
	public void is_service() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.SERVICE);
		
		assertThat(resource.isService()).isTrue();
	}
	
	@Test
	public void get_icon_home_success() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setKey(ProjectResource.MAIN_KEY);
		
		assertThat(resource.getIcon()).isEqualTo("fas home");
	}
	
	@Test
	public void get_icon_home_not_at_root() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(1);
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.WEB);
		resource.setKey(ProjectResource.MAIN_KEY);
		
		assertThat(resource.getIcon()).isNotEqualTo("fas home");
	}
	
	@Test
	public void get_icon_home_not_program() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(ProjectResourceType.FUNCTION);
		resource.setKey(ProjectResource.MAIN_KEY);
		
		assertThat(resource.getIcon()).isNotEqualTo("fas home");
	}
	
	@Test
	public void get_icon_home_name_is_not_readme() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(ProjectResourceType.FUNCTION);
		resource.setKey("a");
		
		assertThat(resource.getIcon()).isNotEqualTo("home");
	}
	
	@Test
	public void get_icon_program_web() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.WEB);
		
		assertThat(resource.getIcon()).isEqualTo("fab firefox");
	}
	
	@Test
	public void get_icon_program_android() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.ANDROID);
		
		assertThat(resource.getIcon()).isEqualTo("fab android");
	}
	
	@Test
	public void get_icon_program_ios() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.IOS);
		
		assertThat(resource.getIcon()).isEqualTo("fab apple");
	}
	
	@Test
	public void get_icon_program_wechat() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.WECHAT_MINI_APP);
		
		assertThat(resource.getIcon()).isEqualTo("fab weixin");
	}
	
	@Test
	public void get_icon_program_alipay() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.ALIPAY_MINI_APP);
		
		assertThat(resource.getIcon()).isEqualTo("fab alipay");
	}
	
	@Test
	public void get_icon_program_quickapp() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.QUICK_APP);
		
		// 目前没有找到合适的图标，先返回空字符串
		assertThat(resource.getIcon()).isEqualTo("");
	}
	
	@Test
	public void get_icon_function() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FUNCTION);
		
		assertThat(resource.getIcon()).isEqualTo("far folder");
	}
	
	@Test
	public void get_icon_program_templet() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PROGRAM_TEMPLET);
		
		assertThat(resource.getIcon()).isEqualTo("far newspaper");
	}
	
	@Test
	public void get_icon_readme() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FILE);
		resource.setKey(ProjectResource.README_KEY);
		
		assertThat(resource.getIcon()).isEqualTo("fas book-open");
	}
	
	@Test
	public void get_icon_service() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.SERVICE);
		
		assertThat(resource.getIcon()).isEqualTo("fas plug");
	}
	
	@Test
	public void get_file_name_program() {
		ProjectResource resource = new ProjectResource();
		resource.setKey("key");
		resource.setResourceType(ProjectResourceType.PROGRAM);
		
		assertThat(resource.getFileName()).isEqualTo("key.page.json");
	}
	
	@Test
	public void get_file_name_program_template() {
		ProjectResource resource = new ProjectResource();
		resource.setKey("key");
		resource.setResourceType(ProjectResourceType.PROGRAM_TEMPLET);
		
		assertThat(resource.getFileName()).isEqualTo("key.page.tmpl.json");
	}
	
	@Test
	public void get_file_name_service() {
		ProjectResource resource = new ProjectResource();
		resource.setKey("key");
		resource.setResourceType(ProjectResourceType.SERVICE);
		
		assertThat(resource.getFileName()).isEqualTo("key.api.json");
	}
	
	@Test
	public void get_file_name_file() {
		ProjectResource resource = new ProjectResource();
		resource.setName("name.x");
		resource.setResourceType(ProjectResourceType.FILE);
		
		assertThat(resource.getFileName()).isEqualTo("name.x");
	}

}
