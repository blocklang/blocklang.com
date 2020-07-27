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
import com.blocklang.core.runner.common.TaskLogger;
import com.blocklang.core.service.PropertyService;
import com.blocklang.marketplace.dao.GitRepoPublishTaskDao;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.blocklang.marketplace.model.GitRepoPublishTask;
import com.blocklang.marketplace.runner.action.BuildIdeRepoAction;
import com.blocklang.marketplace.runner.action.GetRepoConfigAction;
import com.blocklang.marketplace.runner.action.ParseServiceApiRepoAction;
import com.blocklang.marketplace.runner.action.ParseWebApiApiRepoAction;
import com.blocklang.marketplace.runner.action.ParseWidgetApiRepoAction;
import com.blocklang.marketplace.runner.action.PersistComponentRepoAction;
import com.blocklang.marketplace.runner.action.PersistServiceApiRepoAction;
import com.blocklang.marketplace.runner.action.PersistWebApiApiRepoAction;
import com.blocklang.marketplace.runner.action.PersistWidgetApiRepoAction;
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
	private GitRepoPublishTaskDao gitRepoPublishTaskDao;
	
	@Async
	@Override
	public void asyncPublish(GitRepoPublishTask publishTask) {
		this.publish(publishTask);
	}
	
	@Override
	public void publish(GitRepoPublishTask publishTask) {
		StopWatch watch = StopWatch.createStarted();
		
		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH, "");
		MarketplaceStore store = new MarketplaceStore(dataRootPath, publishTask.getGitUrl());
		// 即使发布时涉及到多个仓库，但是日志只记录在发起仓库中
		CliLogger logger = new TaskLogger(store.getLogFilePath());
		logger.enableSendStompMessage(publishTask.getId(), messagingTemplate, STOMP_DESTIONATION_PREFIX);
		
		// 添加日志文件信息
		String logFileName = store.getLogFileName();
		publishTask.setLogFileName(logFileName);
		gitRepoPublishTaskDao.save(publishTask);
		
		ExecutionContext context = new DefaultExecutionContext();
		context.setLogger(logger);
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue(ExecutionContext.PUBLISH_TASK, publishTask);
		context.putValue(ExecutionContext.DATA_ROOT_PATH, dataRootPath);
		
		boolean success = runTask(context);
		
		// 添加发布结果
		ReleaseResult releaseResult = success ? ReleaseResult.PASSED : ReleaseResult.FAILED;
		publishTask.setPublishResult(releaseResult);
		gitRepoPublishTaskDao.save(publishTask);
		
		watch.stop();
		long seconds = watch.getTime(TimeUnit.SECONDS);
		logger.info("成功注册到组件市场");
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
	 *               uses: BuildIdeRepoAction
	 *               
	 *             - id: build_web_api
	 *               if: repo == "WebApi" && category == "IDE"
	 *               uses: BuildIdeRepoAction
	 *               
	 *             - id: parse_service_api
	 *               if: repo == "Service"
	 *               uses: ParseServiceApiRepoAction
	 *               
	 *               id: spersist_service_api
	 *               if: repo == "Service"
	 *               uses: PersistServiceApiRepoAction
	 *               
	 *         
	 *     job2
	 *         needs: job1
	 *         if: repo!="API" && (category == "Widget" || category == "WebApi")
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
	 *      job
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
	 * 
	 * Workflow workflow = new Workflow("publishRepo");
	 *		Job job = new Job("publishRepoJob");
	 *		
	 *			Step checkoutGitRepo = new Step("checkoutGitRepo");
	 *			CheckoutAction checkoutAction = new CheckoutAction(context);
	 *			checkoutGitRepo.setUses(checkoutAction);
	 *		job.addStep(checkoutGitRepo);
	 *		
	 *			Step getRepoConfig = new Step("getRepoConfig");
	 *			GetRepoConfigAction getRepoConfigAction = new GetRepoConfigAction(context);
	 *			getRepoConfig.setUses(getRepoConfigAction);
	 *		job.addStep(getRepoConfig);
	 *		
	 *			Step 
	 *		
	 *	workflow.addJob(job);
	 *	
	 *	Runner runner = new Runner();
	 *	return runner.run(workflow);
	 */
	private boolean runTask(ExecutionContext context) {
		CheckoutAction checkout = new CheckoutAction(context);
		if (!checkout.run()) {
			return false;
		}

		GetRepoConfigAction getRepoConfig = new GetRepoConfigAction(context);
		if (!getRepoConfig.run()) {
			return false;
		}
		
		RepoConfigJson repoConfig = (RepoConfigJson) getRepoConfig.getOutput(GetRepoConfigAction.OUTPUT_REPO_CONFIG);
		String repoType = repoConfig.getRepo();
		String repoCategory = repoConfig.getCategory();
		
		if (repoType.equals("IDE")
				&& (repoCategory.equals("Widget") || repoCategory.equals("WebApi"))) {
			BuildIdeRepoAction buildIdeRepo = new BuildIdeRepoAction(context);
			if (!buildIdeRepo.run()) {
				return false;
			}
		}
		
		if(repoType.equals("IDE") || repoType.equals("PROD")) {
			String apiGitUrl = repoConfig.getApi().getGit();
			String dataRootPath = context.getStringValue(ExecutionContext.DATA_ROOT_PATH);
			MarketplaceStore store = new MarketplaceStore(dataRootPath, apiGitUrl);
			context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
			context.putValue(ExecutionContext.GIT_URL, apiGitUrl);
			
			CheckoutAction checkoutApiRepo = new CheckoutAction(context);
			if (!checkoutApiRepo.run()) {
				return false;
			}
			
			// 在此 action 中要确认是否有效的 api 仓库
			GetRepoConfigAction apiRepoGetRepoConfig = new GetRepoConfigAction(context);
			if(!apiRepoGetRepoConfig.run()) {
				return false;
			}
			
			RepoConfigJson apiRepoConfig = (RepoConfigJson) apiRepoGetRepoConfig.getOutput(GetRepoConfigAction.OUTPUT_REPO_CONFIG);
			String apiRepoCategory = apiRepoConfig.getCategory();
			
			if (apiRepoCategory.equals("Widget")) {
				ParseWidgetApiRepoAction parseWidgetApi = new ParseWidgetApiRepoAction(context);
				if (!parseWidgetApi.run()) {
					return false;
				}
				PersistWidgetApiRepoAction persistWidgetApi = new PersistWidgetApiRepoAction(context);
				if (!persistWidgetApi.run()) {
					return false;
				}
			} else if (apiRepoCategory.equals("WebApi")) {
				ParseWebApiApiRepoAction parseWebApi = new ParseWebApiApiRepoAction(context);
				if (!parseWebApi.run()) {
					return false;
				}
				PersistWebApiApiRepoAction persistWebApi = new PersistWebApiApiRepoAction(context);
				if (!persistWebApi.run()) {
					return false;
				}
			}
			
			// 存储 component repo 信息
			GitRepoPublishTask publishTask = context.getValue(ExecutionContext.PUBLISH_TASK, GitRepoPublishTask.class);
			MarketplaceStore componentRepoStore = new MarketplaceStore(dataRootPath, publishTask.getGitUrl());
			context.putValue(ExecutionContext.MARKETPLACE_STORE, componentRepoStore);
			
			PersistComponentRepoAction persistComponentRepo = new PersistComponentRepoAction(context);
			if(!persistComponentRepo.run()) {
				return false;
			}
		}
		
		if (repoType.equals("API")) {
			if (repoCategory.equals("Service")) {
				ParseServiceApiRepoAction parseServiceApi = new ParseServiceApiRepoAction(context);
				if (!parseServiceApi.run()) {
					return false;
				}
				PersistServiceApiRepoAction persistServiceApi = new PersistServiceApiRepoAction(context);
				if (!persistServiceApi.run()) {
					return false;
				}
			} else if (repoCategory.equals("Widget")) {
				ParseWidgetApiRepoAction parseWidgetApi = new ParseWidgetApiRepoAction(context);
				if (!parseWidgetApi.run()) {
					return false;
				}
				PersistWidgetApiRepoAction persistWidgetApi = new PersistWidgetApiRepoAction(context);
				if (!persistWidgetApi.run()) {
					return false;
				}
			} else if (repoCategory.equals("WebApi")) {
				ParseWebApiApiRepoAction parseWebApi = new ParseWebApiApiRepoAction(context);
				if (!parseWebApi.run()) {
					return false;
				}
				PersistWebApiApiRepoAction persistWebApi = new PersistWebApiApiRepoAction(context);
				if (!persistWebApi.run()) {
					return false;
				}
			}
		}
		
		return true;
	}

}
