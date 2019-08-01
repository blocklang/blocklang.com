import * as css from './ViewProject.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import * as c from '../../className';
import { Project, ProjectGroup, CommitInfo, ProjectDependence } from '../../interfaces';
import Spinner from '../../widgets/spinner';
import { isEmpty } from '../../util';
import Exception from '../error/Exception';
import ProjectHeader from '../widgets/ProjectHeader';
import messageBundle from '../../nls/main';

import Link from '@dojo/framework/routing/Link';
import { ProjectResourcePathPayload } from '../../processes/interfaces';
import BreadcrumbItem from './widgets/BreadcrumbItem';
import LatestCommitInfo from './widgets/LatestCommitInfo';

export interface ViewProjectDependenceProperties {
	loggedUsername: string;
	project: Project;
	sourceId: number;
	pathes: ProjectGroup[];
	dependences: ProjectDependence[];
	latestCommitInfo: CommitInfo;
	onOpenGroup: (opt: ProjectResourcePathPayload) => void;
}

@theme(css)
export default class ViewProjectDependence extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectDependenceProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { project } = this.properties;
		if (!project) {
			return v('div', { classes: [c.mt_5] }, [w(Spinner, {})]);
		}

		if (this._isNotFound()) {
			return w(Exception, { type: '404' });
		}

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderNavigation(),
			this._renderDependenceBlock()
		]);
	}

	private _isNotFound() {
		const { project } = this.properties;
		return isEmpty(project);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderNavigation() {
		return v('div', { classes: [c.d_flex, c.justify_content_between, c.mb_2] }, [
			v('div', {}, [this._renderBreadcrumb()])
		]);
	}

	private _renderBreadcrumb() {
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

	private _renderDependenceBlock() {
		const { latestCommitInfo } = this.properties;

		return v('div', { classes: [c.card, !latestCommitInfo ? c.border_top_0 : undefined] }, [
			w(LatestCommitInfo, { latestCommitInfo }) // 最近提交信息区
		]);
	}
}
