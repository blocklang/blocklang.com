import { Repository, RepositoryResourceGroup } from '../../../interfaces';
import { RepositoryResourcePathPayload } from '../../../processes/interfaces';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { w, v } from '@dojo/framework/core/vdom';
import Link from '@dojo/framework/routing/Link';
import global from '@dojo/framework/shim/global';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './GoToParentGroupLink.m.css';

interface GoToParentGroupLinkProperties {
	repository: Repository;
	parentGroups: RepositoryResourceGroup[];
	onGoToGroup: (opt: RepositoryResourcePathPayload) => void;
}

// TODO: 确认 Link 部件，是不是本身就支持此操作
@theme(css)
export default class GoToParentGroupLink extends ThemedMixin(I18nMixin(WidgetBase))<GoToParentGroupLinkProperties> {
	protected render() {
		const { repository, parentGroups = [] } = this.properties;
		if (parentGroups.length === 1) {
			// 上一级是根目录
			return w(
				Link,
				{
					classes: [c.px_2],
					title: '到上级目录',
					to: 'view-repo',
					params: { owner: repository.createUserName, repo: repository.name },
				},
				['..']
			);
		} else {
			return v(
				'a',
				{
					href: `/${repository.createUserName}/${repository.name}/groups/${this._getParentPath()}`,
					// 因为 dojo 5.0 的 route 不支持通配符，这里尝试实现类似效果
					onclick: this._onGoToGroup,
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
		const { repository } = this.properties;
		event.stopPropagation();
		event.preventDefault();
		const parentPath = this._getParentPath();
		this.properties.onGoToGroup({ owner: repository.createUserName, repo: repository.name, parentPath });
		global.window.history.pushState(
			{},
			'',
			`/${repository.createUserName}/${repository.name}/groups/${parentPath}`
		);
		return false;
	}
}
