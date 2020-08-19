import { RepositoryResourceGroup, Repository } from '../../../interfaces';
import { RepositoryResourcePathPayload } from '../../../processes/interfaces';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import global from '@dojo/framework/shim/global';
import { v } from '@dojo/framework/core/vdom';
import * as c from '@blocklang/bootstrap-classes';
import * as css from './BreadcrumbItem.m.css';

interface BreadcrumbItemProperties {
	repository: Repository;
	parentGroup: RepositoryResourceGroup;
	onGoToGroup: (opt: RepositoryResourcePathPayload) => void;
}

@theme(css)
export default class BreadcrumbItem extends ThemedMixin(I18nMixin(WidgetBase))<BreadcrumbItemProperties> {
	protected render() {
		const { repository, parentGroup } = this.properties;

		return v('li', { classes: [c.breadcrumb_item] }, [
			v(
				'a',
				{
					href: `/${repository.createUserName}/${repository.name}/groups/${parentGroup.path.substring(1)}`,
					// 因为 dojo 5.0 的 route 不支持通配符，这里尝试实现类似效果
					onclick: this._onGoToGroup,
				},
				[`${parentGroup.name}`]
			),
		]);
	}

	private _onGoToGroup(event: any) {
		const { repository, parentGroup } = this.properties;
		event.stopPropagation();
		event.preventDefault();
		this.properties.onGoToGroup({
			owner: repository.createUserName,
			repo: repository.name,
			parentPath: parentGroup.path.substring(1),
		});
		global.window.history.pushState(
			{},
			'',
			`/${repository.createUserName}/${repository.name}/groups/${parentGroup.path.substring(1)}`
		);
		return false;
	}
}
