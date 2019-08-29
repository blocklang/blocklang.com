import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { Project, ProjectGroup } from '../../../interfaces';
import * as css from './ProjectResourceBreadcrumb.m.css';
import { v, w } from '@dojo/framework/core/vdom';
import Link from '@dojo/framework/routing/Link';
import * as c from '../../../className';
import BreadcrumbItem from './BreadcrumbItem';
import { ProjectResourcePathPayload } from '../../../processes/interfaces';

export interface ProjectResourceBreadcrumbProperties {
	project: Project;
	pathes: ProjectGroup[];
	onOpenGroup: (opt: ProjectResourcePathPayload) => void;
}

@theme(css)
export default class ProjectResourceBreadcrumb extends ThemedMixin(I18nMixin(WidgetBase))<
	ProjectResourceBreadcrumbProperties
> {
	protected render() {
		const { project, pathes = [] } = this.properties;

		return v('nav', { classes: [c.d_inline_block], 'aria-label': 'breadcrumb' }, [
			v('ol', { classes: [c.breadcrumb, css.navOl] }, [
				// 项目名
				v('li', { classes: [c.breadcrumb_item] }, [
					w(
						Link,
						{
							to: 'view-project',
							params: { owner: project.createUserName, project: project.name },
							classes: [c.font_weight_bold]
						},
						[`${project.name}`]
					)
				]),
				...pathes.map((item, index, array) => {
					if (index !== array.length - 1) {
						return w(BreadcrumbItem, { project, parentGroup: item, onGoToGroup: this._onGoToGroup });
					} else {
						// 如果是最后一个元素
						return v('li', { classes: [c.breadcrumb_item, c.active] }, [
							v('strong', { classes: [c.pr_2] }, [`${item.name}`])
						]);
					}
				})
			])
		]);
	}

	private _onGoToGroup(opt: ProjectResourcePathPayload) {
		this.properties.onOpenGroup(opt);
	}
}
