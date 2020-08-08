import * as css from './ViewProject.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { w } from '@dojo/framework/core/vdom';
import PageDesigner from '@blocklang/page-designer';
import { User, Project, ProjectResource, ProjectResourceGroup } from '../../interfaces';
import { canReadPage, canEditPage } from '../../permission';
import Spinner from '../../widgets/spinner';
import global from '@dojo/framework/shim/global';
import { ProjectResourcePathPayload } from '../../processes/interfaces';
import { getHeaders } from '../../processes/utils';

export interface ViewProjectPageProperties {
	loginUser: User;
	project: Project;
	resource: ProjectResource; // 项目资源，此处指一个页面
	groups: ProjectResourceGroup[];
	onGotoGroup: (opt: ProjectResourcePathPayload) => void;
}

@theme(css)
export default class ViewProjectPage extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectPageProperties> {
	protected render() {
		const { project, loginUser, resource, groups, onGotoGroup } = this.properties;
		if (!project || !resource) {
			return w(Spinner, {});
		}

		const projectId = project.id;
		const pageId = resource.id;

		return w(PageDesigner, {
			project,
			user: { name: loginUser.loginName, avatar: loginUser.avatarUrl },
			page: { id: resource.id, key: resource.key, name: resource.name, appType: resource.appType },
			permission: { canRead: canReadPage(project.accessLevel), canWrite: canEditPage(project.accessLevel) },
			pathes: groups,
			urls: {
				fetchApiRepoWidgets: `/designer/projects/${projectId}/dependences/widgets`,
				fetchPageModel: `/designer/pages/${pageId}/model`,
				savePageModel: `/designer/pages/${pageId}/model`,
				fetchIdeDependenceInfos: `/designer/projects/${projectId}/dependences?repo=ide`,
				externalScriptAndCssWebsite: '', // 不能为 "/"
				fetchApiRepoServices: '',
				fetchApiRepoFunctions: '',
				customFetchHeaders: () => {
					return getHeaders();
				},
			},
			routes: {
				profile: 'profile',
				parentGroup: 'view-project',
				// 当 dojo 路由支持通配符后，删除此段代码
				gotoGroup: (owner: string, project: string, parentPath: string) => {
					onGotoGroup({ owner, project, parentPath });
					global.window.history.pushState({}, '', `/${owner}/${project}/groups/${parentPath}`);
				},
			},
		});
	}
}
