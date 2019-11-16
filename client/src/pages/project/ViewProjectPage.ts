import * as css from './ViewProject.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { w } from '@dojo/framework/core/vdom';
import PageDesigner from 'page-designer/PageDesigner';
import { User, Project, ProjectResource, ProjectResourceGroup } from '../../interfaces';
import { canReadPage, canEditPage } from '../../permission';
import Spinner from '../../widgets/spinner';
import global from '@dojo/framework/shim/global';
import { ProjectResourcePathPayload } from '../../processes/interfaces';

export interface ViewProjectPageProperties {
	loginUser: User;
	project: Project;
	resource: ProjectResource; // 项目资源，此处指一个页面
	groups: ProjectResourceGroup[];
	onGotoGroup: (opt: ProjectResourcePathPayload) => void;
}

@theme(css)
export default class ViewProjectPage extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectPageProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { project, loginUser, resource, groups, onGotoGroup } = this.properties;
		if (!resource) {
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
				fetchApiRepoWidgets: `/designer/prjects/${projectId}/dependences/widgets`,
				fetchPageModel: `/designer/pages/${pageId}/model`,
				fetchIdeDependenceInfos: `/designer/projects/${projectId}/dependences?category=ide`,
				externalScriptAndCssWebsite: '/'
			},
			routes: {
				profile: 'profile',
				parentGroup: 'view-project',
				// 当 dojo 路由支持通配符后，删除此段代码
				gotoGroup: (owner: string, project: string, parentPath: string) => {
					onGotoGroup({ owner, project, parentPath });
					global.window.history.pushState({}, '', `/${owner}/${project}/groups/${parentPath}`);
				}
			}
		});
	}
}
