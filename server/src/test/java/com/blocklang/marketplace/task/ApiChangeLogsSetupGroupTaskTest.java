package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

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
		publishTask.setGitUrl("https://github.com/blocklang/blocklang.com.git");
		publishTask.setStartTime(LocalDateTime.now());
		publishTask.setPublishResult(ReleaseResult.INITED);
		
		context = new MarketplacePublishContext(folder, publishTask);
	
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
