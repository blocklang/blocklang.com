import { Project, ProjectGroup } from '../../../interfaces';
import { ProjectResourcePathPayload } from '../../../processes/interfaces';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { w, v } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';
import global from '@dojo/framework/shim/global';

import * as c from '../../../className';
import * as css from './GoToParentGroupLink.m.css';

interface GoToParentGroupLinkProperties {
	project: Project;
	parentGroups: ProjectGroup[];
	onGoToGroup: (opt: ProjectResourcePathPayload) => void;
}

// TODO: 确认 Link 部件，是不是本身就支持此操作
@theme(css)
export default class GoToParentGroupLink extends ThemedMixin(I18nMixin(WidgetBase))<GoToParentGroupLinkProperties> {
	protected render() {
		const { project, parentGroups = [] } = this.properties;
		if (parentGroups.length === 1) {
			// 上一级是根目录
			return w(
				Link,
				{
					classes: [c.px_2],
					title: '到上级目录',
					to: 'view-project',
					params: { owner: project.createUserName, project: project.name }
				},
				['..']
			);
		} else {
			return v(
				'a',
				{
					href: `/${project.createUserName}/${project.name}/groups/${this._getParentPath()}`,
					// 因为 dojo 5.0 的 route 不支持通配符，这里尝试实现类似效果
					onclick: this._onGoToGroup
				},
				['..']
			);
		}
	}

	private _getParentPath() {
		const { parentGroups = [] } = this.properties;

		if (parentGroups.length < 2) {
			return '';
		}

		return parentGroups[parentGroups.length - 2].path.substring(1);
	}

	private _onGoToGroup(event: any) {
		const { project } = this.properties;
		event.stopPropagation();
		event.preventDefault();
		const parentPath = this._getParentPath();
		this.properties.onGoToGroup({ owner: project.createUserName, project: project.name, parentPath });
		global.window.history.pushState({}, '', `/${project.createUserName}/${project.name}/groups/${parentPath}`);
		return false;
	}
}
