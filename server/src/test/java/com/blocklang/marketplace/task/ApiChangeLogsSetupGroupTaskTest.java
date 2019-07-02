package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.lib.Constants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.data.changelog.ComponentChangeLogs;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public class ApiChangeLogsSetupGroupTaskTest extends AbstractServiceTest {

	private MarketplacePublishContext context;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;

	@Before
	public void setup() throws IOException {
		String folder = temp.newFolder().getPath();
		
		ComponentRepoPublishTask publishTask = new ComponentRepoPublishTask();
		publishTask.setGitUrl("https://a.com/user/component.repo.git");
		publishTask.setStartTime(LocalDateTime.now());
		publishTask.setPublishResult(ReleaseResult.INITED);
		publishTask.setCreateUserId(1);
		publishTask.setCreateTime(LocalDateTime.now());
		
		context = new MarketplacePublishContext(folder, publishTask);
		context.parseApiGitUrl("https://a.com/user/api.repo.git");
		
		ApiJson apiJson = new ApiJson();
		apiJson.setName("api-a");
		apiJson.setVersion("0.1.0");
		apiJson.setDisplayName("API A");
		apiJson.setDescription("api description");
		apiJson.setCategory(RepoCategory.WIDGET.getValue());
		context.setApiJson(apiJson);
		
		context.setAllApiRepoTagNames(Collections.singletonList(Constants.R_TAGS + "v0.1.0"));
		context.setApiRepoVersions(Arrays.asList(new String[] {"0.1.0"}));
		
		ComponentJson componentJson = new ComponentJson();
		componentJson.setName("component-a");
		componentJson.setVersion("0.1.0");
		componentJson.setDisplayName("Component A");
		componentJson.setDescription("component description");
		componentJson.setCategory(RepoCategory.WIDGET.getValue());
		componentJson.setLanguage(Language.TYPESCRIPT.getValue());
		context.setComponentJson(componentJson);
		
		List<ComponentChangeLogs> changeLogs = new ArrayList<ComponentChangeLogs>();
		context.setChangeLogs(changeLogs);
	}
	
	@Test
	public void run_one_change_new_widget() {
		ApiChangeLogsSetupGroupTask task = new ApiChangeLogsSetupGroupTask(
				context,
				componentRepoDao,
				componentRepoVersionDao,
				apiRepoDao,
				apiRepoVersionDao);
		assertThat(task.run()).isPresent();
//		task.setComponentRepo();
//		task.setComponentRepoVersion();
//		task.setApiRepo();
//		task.setApiRepoVersions();
		// 要包含所有的 changelog（注意，是指定版本的下的所有 changelog 文件）
		// 约定以当前版中的 changelog 为准，不用逐个版本的获取 changelog 文件
		// changelog 列表中是以版本号从小到大排列的
		
	}
	
}
