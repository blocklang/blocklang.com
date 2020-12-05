import * as css from './ViewRepository.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { w } from '@dojo/framework/core/vdom';
import PageDesigner from '@blocklang/page-designer';
import { User, Repository, RepositoryResource, RepositoryResourceGroup } from '../../interfaces';
import { canReadPage, canEditPage } from '../../permission';
import Spinner from '../../widgets/spinner';
import global from '@dojo/framework/shim/global';
import { RepositoryResourcePathPayload } from '../../processes/interfaces';
import { getHeaders } from '../../processes/utils';

export interface ViewRepositoryPageProperties {
	loginUser: User;
	repository: Repository;
	projectId: number; // 资源所属项目标识，注意不是仓库标识
	resource: RepositoryResource; // 仓库资源，此处指一个页面
	groups: RepositoryResourceGroup[];
	onGotoGroup: (opt: RepositoryResourcePathPayload) => void;
}

@theme(css)
export default class ViewRepositoryPage extends ThemedMixin(I18nMixin(WidgetBase))<ViewRepositoryPageProperties> {
	protected render() {
		const { repository, loginUser, projectId, resource, groups, onGotoGroup } = this.properties;

		console.log('repository', repository);
		console.log('resource', resource);

		if (!repository || !resource || resource.id === -1) {
			return w(Spinner, { key: 'spinner' });
		}

		const pageId = resource.id;

		// 约定页面必须按照“仓库-项目-[分组]-页面” 方式存储
		return w(PageDesigner, {
			key: 'pageDesigner',
			project: repository,
			user: { name: loginUser.loginName, avatar: loginUser.avatarUrl },
			page: {
				id: resource.id,
				key: resource.key,
				name: resource.name,
				appType: resource.appType,
				deviceType: resource.deviceType,
			},
			permission: { canRead: canReadPage(repository.accessLevel), canWrite: canEditPage(repository.accessLevel) },
			pathes: groups,
			urls: {
				fetchApiRepoWidgets: `/designer/projects/${projectId}/dependencies/widgets`, // 依赖是与项目关联的，不是与仓库关联的
				fetchPageModel: `/designer/pages/${pageId}/model`,
				savePageModel: `/designer/pages/${pageId}/model`,
				fetchIdeDependencyInfos: `/designer/projects/${projectId}/dependencies?repo=ide`,
				externalScriptAndCssWebsite: '', // 不能为 "/"
				fetchApiRepoServices: '',
				fetchApiRepoFunctions: '',
				customFetchHeaders: () => {
					return getHeaders();
				},
			},
			routes: {
				profile: 'profile',
				parentGroup: 'view-repo',
				// 当 dojo 路由支持通配符后，删除此段代码
				gotoGroup: (owner: string, repository: string, parentPath: string) => {
					onGotoGroup({ owner, repo: repository, parentPath });
					global.window.history.pushState({}, '', `/${owner}/${repository}/groups/${parentPath}`);
				},
			},
		});
	}
}
