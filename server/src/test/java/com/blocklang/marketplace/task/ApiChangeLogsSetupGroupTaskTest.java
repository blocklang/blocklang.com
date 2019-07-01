package com.blocklang.marketplace.task;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ApiChangeLogsSetupGroupTaskTest {

	
	private MarketplacePublishContext context;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Before
	public void setup() throws IOException {
		String folder = temp.newFolder().getPath();
		context = new MarketplacePublishContext(folder, "https://github.com/blocklang/blocklang.com.git");
	}
	
	@Test
	public void run_one_change_new_widget() {
		//ApiChangeLogsSetupGroupTask task = new ApiChangeLogsSetupGroupTask(context);
		
//		task.setComponentRepo();
//		task.setComponentRepoVersion();
//		task.setApiRepo();
//		task.setApiRepoVersions();
		// 要包含所有的 changelog（注意，是指定版本的下的所有 changelog 文件）
		// 约定以当前版中的 changelog 为准，不用逐个版本的获取 changelog 文件
		// changelog 列表中是以版本号从小到大排列的
		
		
		//assertThat(task.run()).isPresent();
	}
	
}
