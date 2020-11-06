import { tsx, create } from '@dojo/framework/core/vdom';
import icache from '@dojo/framework/core/middleware/icache';
import i18n from '@dojo/framework/core/middleware/i18n';
import store from '../../../store';
import mainBundle from '../../../nls/main';
import bundle from './nls/ViewRepositoryBuild';
import RepositoryHeader from '../../widgets/RepositoryHeader';
import { LoginStatus, ResourceType } from '../../../constant';
import Exception from '../../error/Exception';
import { getRepositoryProcess, getRepositoryGroupChildrenProcess } from '../../../processes/repositoryProcesses';
import * as c from '@blocklang/bootstrap-classes';
import { getAppTypeName } from '../../../util';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import * as copy from 'copy-to-clipboard';
import "popper.js";
import * as $ from 'jquery';

export interface ViewRepositoryBuildProperties {
	owner: string;
	repoName: string;
}

const factory = create({ store, i18n, icache }).properties<ViewRepositoryBuildProperties>();

export default factory(function ViewRepositoryBuild({ properties, middleware: { store, i18n, icache } }) {
	const {messages: mainMessage} = i18n.localize(mainBundle);
	const { messages } = i18n.localize(bundle);
	
	console.log(messages);

	const { get, path, executor } = store;

	const { status: loginStatus } = get(path('user'));
	if (loginStatus !== LoginStatus.LOGINED) {
		return <Exception type="403" />;
	}

	const { owner, repoName } = properties();
	console.log(owner, repoName)
	const repository = get(path("repository"));
	if(!repository) {
		executor(getRepositoryProcess)({owner, repo: repoName});
		return;
	}

	const childResources = get(path("childResources"));
	if(!childResources) {
		executor(getRepositoryGroupChildrenProcess)({owner, repo: repoName});
		return;
	}

	// TODO: 正在加载
	return (
		<div classes={[c.container]}>
			<RepositoryHeader repository={repository} privateRepositoryTitle={mainMessage.privateRepositoryTitle}/>
		
			<div classes={[c.font_weight_bolder, c.mb_2]}>项目发布</div>
			<div>
				{
					childResources.filter(item => item.resourceType === ResourceType.Project)
						.map(item => <div classes={[c.card]}>
							<div classes={[c.card_header]}>
								<span classes={[c.font_weight_bold]}>{item.key}</span>  {getAppTypeName(item.appType)}
							</div>
							<ul classes={[c.list_group, c.list_group_flush]}>
								<li classes={[c.list_group_item]}>
									<form classes={[c.form_group]}>
										<label for="gitUrl">微信小程序</label>
										<div classes={[c.input_group]}>
											<div classes={[c.input_group_prepend]}>
												<span classes={[c.input_group_text]}>使用 git 克隆源码</span>
											</div>
											<input 
												type="text" 
												classes={[c.form_control]} 
												id="gitUrl" 
												readOnly 
												value={`https://blocklang.com/${owner}/${repoName}/${item.key}/weapp.git`} 
												styles={{width: "600px"}}/>
											<div classes={[c.input_group_append]}>
												<button 
													id="copyButton"
													type="button" 
													classes={[c.btn, c.btn_outline_secondary]}
													data-toggle="tooltip"
													data-placement="top"
													onmouseout={()=>{
														($('#copyButton') as any).tooltip('dispose');
													}}
													onclick={()=>{
														copy(`https://blocklang.com/${owner}/${repoName}/${item.key}/weapp.git`);
														($('#copyButton') as any).tooltip({title: "已复制"});
														($('#copyButton') as any).tooltip('show');
													}}>
													<FontAwesomeIcon icon="clipboard"/>
												</button>
											</div>
										</div>
									</form>
									 
								</li>
							</ul>
						</div>)
				}
			</div>
		</div>
	);
});
