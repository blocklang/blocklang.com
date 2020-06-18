package com.blocklang.marketplace.service.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.runner.action.CheckoutAction;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.runner.common.Job;
import com.blocklang.core.runner.common.Runner;
import com.blocklang.core.runner.common.Step;
import com.blocklang.core.runner.common.TaskLogger;
import com.blocklang.core.runner.common.Workflow;
import com.blocklang.core.service.PropertyService;
import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.runner.action.GetRepoConfigAction;
import com.blocklang.marketplace.service.RepoPublishService;
import com.blocklang.release.constant.ReleaseResult;

@Service
public class RepoPublishServiceImpl implements RepoPublishService {

	private static final String STOMP_DESTIONATION_PREFIX = "/topic/publish/";
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	
	@Async
	@Override
	public void publish(ComponentRepoPublishTask publishTask) {
		StopWatch watch = StopWatch.createStarted();
		
		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH, "");
		MarketplaceStore store = new MarketplaceStore(dataRootPath, publishTask.getGitUrl());
		// 即使发布时涉及到多个仓库，但是日志只记录在发起仓库中
		CliLogger logger = new TaskLogger(store.getLogFilePath());
		logger.enableSendStompMessage(publishTask.getId(), messagingTemplate, STOMP_DESTIONATION_PREFIX);
		
		// 添加日志文件信息
		String logFileName = store.getLogFileName();
		publishTask.setLogFileName(logFileName);
		componentRepoPublishTaskDao.save(publishTask);
		
		ExecutionContext context = new DefaultExecutionContext();
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue(ExecutionContext.PUBLISH_TASK, publishTask);
		
		boolean success = runTask(context);
		
		// 添加发布结果
		ReleaseResult releaseResult = success ? ReleaseResult.PASSED : ReleaseResult.FAILED;
		publishTask.setPublishResult(releaseResult);
		componentRepoPublishTaskDao.save(publishTask);
		
		watch.stop();
		long seconds = watch.getTime(TimeUnit.SECONDS);
		logger.info("共耗时 {0} 秒", seconds);
		
		logger.finished(releaseResult);
	}

	/**
	 * <pre>
	 * jobs
	 *     job1
	 *         outputs:
	 *             repo: ${{}}
	 *             category: ${{}}
	 *         steps
	 *             - uses: CheckoutAction
	 *             
	 *             - uses: GetRepoConfigAction
	 *             
	 *             - id: build_dojo_app
	 *               if: repo == "Widget" && category == "IDE"
	 *               uses: BuildDojoAppAction
	 *               
	 *             - id: parse_service_api
	 *               if: repo == "Service"
	 *               uses: ParseServiceApiAction
	 *               
	 *               id: spersist_service_api
	 *               if: repo == "Service"
	 *               uses: PersistServiceApiAction
	 *               
	 *             - id: build_web_api
	 *               if: repo == "WebApi" && category == "IDE"
	 *               uses: BuildWebApiAction
	 *         
	 *     job2
	 *         needs: job1
	 *         if: repo=="IDE" && (category == "Widget" || category == "WebApi")
	 *         steps
	 *             - uses: CheckoutAction
	 *             - uses: GetRepoConfigAction
	 *             
	 *             - id: parse_widget_api
	 *               if: category == "Widget"
	 *             - uses: ParseWidgetApiAction
	 *             
	 *             - id: persist_widget
	 *               if: category == "Widget
	 *               uses: PersistWidgetAction
	 *               
	 *             - id: parse_web_api
	 *               if: category == "WebApi"
	 *             - uses: ParseWebApiAction
	 *               
	 *               id: persist_web_api
	 *               if: category == "WebApi
	 *               uses: PersistWebApiAction
	 *      job3
	 *          needs: job2
	 *          if: repo != "API"
	 *               id: persist_component_repo
	 *               uses: PersistComponentRepoAction
	 * 
	 * </pre>
	 * 
	 * 在 PublishRepoAction 中包含发布 Widget，Service 和 WebApi 的 IDE 版和 API 版仓库。
	 * outputs 为仓库的类型，如果发布的为 Widget 和 WebApi 的 IDE 版仓库，则发布其实现的 API 仓库
	 * 
	 * 
	 * FIXME: 是否有必要支持直接升级 Widget 和 WebApi 的 API 仓库？
	 */
	private boolean runTask(ExecutionContext context) {
		Workflow workflow = new Workflow("publishRepo");
			Job job = new Job("publishRepoJob");
			
				Step checkoutGitRepo = new Step("checkoutGitRepo");
				CheckoutAction checkoutAction = new CheckoutAction(context);
				checkoutGitRepo.setUses(checkoutAction);
			job.addStep(checkoutGitRepo);
			
				Step getRepoConfig = new Step("getRepoConfig");
				GetRepoConfigAction getRepoConfigAction = new GetRepoConfigAction(context);
				getRepoConfig.setUses(getRepoConfigAction);
			job.addStep(getRepoConfig);
			
				Step 
			
		workflow.addJob(job);
		
		Runner runner = new Runner();
		return runner.run(workflow);
	}

}
