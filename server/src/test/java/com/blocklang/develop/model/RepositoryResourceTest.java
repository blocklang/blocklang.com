package com.blocklang.develop.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.blocklang.core.constant.Constant;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;

public class RepositoryResourceTest {

	@Test
	public void is_main_program_success() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setKey(RepositoryResource.MAIN_KEY);
		
		assertThat(resource.isMain()).isTrue();
	}
	
	@Test
	public void is_main_program_at_root_not_main_key() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setKey("a");
		
		assertThat(resource.isMain()).isFalse();
	}
	
	@Test
	public void is_main_program_not_at_root_not_main_key() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(1);
		resource.setKey(RepositoryResource.MAIN_KEY);
		
		assertThat(resource.isMain()).isFalse();
	}
	
	@Test
	public void is_templet() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE_TEMPLET);
		
		assertThat(resource.isTemplet()).isTrue();
	}
	
	@Test
	public void is_group() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.GROUP);
		
		assertThat(resource.isGroup()).isTrue();
	}
	
	@Test
	public void is_page() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE);
		
		assertThat(resource.isPage()).isTrue();
	}
	
	@Test
	public void is_file() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.FILE);
		
		assertThat(resource.isFile()).isTrue();
	}
	
	@Test
	public void is_readme() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.FILE);
		resource.setKey(RepositoryResource.README_KEY);
		
		assertThat(resource.isReadme()).isTrue();
	}
	
	@Test
	public void is_service() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.SERVICE);
		
		assertThat(resource.isService()).isTrue();
	}
	
	@Test
	public void get_icon_home_success() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setKey(RepositoryResource.MAIN_KEY);
		
		assertThat(resource.getIcon()).isEqualTo("fas home");
	}
	
	@Test
	public void get_icon_home_at_project_root() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(1);
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setKey(RepositoryResource.MAIN_KEY);
		
		assertThat(resource.getIcon()).isEqualTo("fas home");
	}
	
	@Test
	public void get_icon_home_not_page() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setKey(RepositoryResource.MAIN_KEY);
		
		assertThat(resource.getIcon()).isNotEqualTo("fas home");
	}
	
	@Test
	public void get_icon_home_name_is_not_readme() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setKey("a");
		
		assertThat(resource.getIcon()).isNotEqualTo("home");
	}
	
	@Test
	public void get_icon_page_web() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		
		assertThat(resource.getIcon()).isEqualTo("fab firefox");
	}
	
//	@Test
//	public void get_icon_page_android() {
//		ProjectResource resource = new ProjectResource();
//		resource.setResourceType(ProjectResourceType.PAGE);
//		resource.setAppType(AppType.ANDROID);
//		
//		assertThat(resource.getIcon()).isEqualTo("fab android");
//	}
//	
//	@Test
//	public void get_icon_page_ios() {
//		ProjectResource resource = new ProjectResource();
//		resource.setResourceType(ProjectResourceType.PAGE);
//		resource.setAppType(AppType.IOS);
//		
//		assertThat(resource.getIcon()).isEqualTo("fab apple");
//	}
//	
//	@Test
//	public void get_icon_page_wechat() {
//		ProjectResource resource = new ProjectResource();
//		resource.setResourceType(ProjectResourceType.PAGE);
//		resource.setAppType(AppType.WECHAT_MINI_APP);
//		
//		assertThat(resource.getIcon()).isEqualTo("fab weixin");
//	}
//	
//	@Test
//	public void get_icon_page_alipay() {
//		ProjectResource resource = new ProjectResource();
//		resource.setResourceType(ProjectResourceType.PAGE);
//		resource.setAppType(AppType.ALIPAY_MINI_APP);
//		
//		assertThat(resource.getIcon()).isEqualTo("fab alipay");
//	}
//	
//	@Test
//	public void get_icon_page_quickapp() {
//		ProjectResource resource = new ProjectResource();
//		resource.setResourceType(ProjectResourceType.PAGE);
//		resource.setAppType(AppType.QUICK_APP);
//		
//		// 目前没有找到合适的图标，先返回空字符串
//		assertThat(resource.getIcon()).isEqualTo("");
//	}
	
	@Test
	public void get_icon_group() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.GROUP);
		
		assertThat(resource.getIcon()).isEqualTo("fas folder");
	}
	
	@Test
	public void get_icon_page_templet() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE_TEMPLET);
		
		assertThat(resource.getIcon()).isEqualTo("far newspaper");
	}
	
	@Test
	public void get_icon_readme() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.FILE);
		resource.setKey(RepositoryResource.README_KEY);
		
		assertThat(resource.getIcon()).isEqualTo("fas book-open");
	}
	
	@Test
	public void get_icon_service() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.SERVICE);
		
		assertThat(resource.getIcon()).isEqualTo("fas plug");
	}
	
	@Test
	public void get_file_name_page() {
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("key");
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		
		assertThat(resource.getFileName()).isEqualTo("key.page.web.json");
	}
	
	@Test
	public void get_file_name_page_template() {
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("key");
		resource.setResourceType(RepositoryResourceType.PAGE_TEMPLET);
		
		assertThat(resource.getFileName()).isEqualTo("key.page.tmpl.json");
	}
	
	@Test
	public void get_file_name_service() {
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("key");
		resource.setResourceType(RepositoryResourceType.SERVICE);
		
		assertThat(resource.getFileName()).isEqualTo("key.api.json");
	}
	
	@Test
	public void get_file_name_file() {
		RepositoryResource resource = new RepositoryResource();
		resource.setName("name.x");
		resource.setResourceType(RepositoryResourceType.FILE);
		
		assertThat(resource.getFileName()).isEqualTo("name.x");
	}

}
