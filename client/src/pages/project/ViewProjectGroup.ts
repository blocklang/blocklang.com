import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import { v, w } from '@dojo/framework/widget-core/d';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import { Project, ProjectResource, CommitInfo, ProjectGroup } from '../../interfaces';
import Moment from '../../widgets/moment';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import { IconName, IconPrefix } from '@fortawesome/fontawesome-svg-core';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';

import * as c from '../../className';
import * as css from './ViewProject.m.css';

import Spinner from '../../widgets/spinner';
import ProjectHeader from '../widgets/ProjectHeader';
import { isEmpty } from '../../util';
import Exception from '../error/Exception';
import { ResourceType, GitFileStatus } from '../../constant';
import { Params } from '@dojo/framework/routing/interfaces';

export interface ViewProjectGroupProperties {
	loggedUsername: string;
	project: Project;
	parentPath: string;
	parentId: number;
	parentGroups: ProjectGroup[];
	projectResources: ProjectResource[];
	latestCommitInfo: CommitInfo;
}

@theme(css)
export default class ViewProjectGroup extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectGroupProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		if (this._isNotFound()) {
			return w(Exception, { type: '404' });
		}

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderNavigation(),
			this._renderTable()
		]);
	}

	private _isNotFound() {
		const { project } = this.properties;
		return isEmpty(project);
	}

	private _isAuthenticated() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
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
			v('div', {}, [this._renderBreadcrumb()]),
			v('div', { classes: [] }, [this._renderNewResourceButtonGroup()])
		]);
	}

	private _renderBreadcrumb() {
		const { project, parentGroups } = this.properties;

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
				...parentGroups.map((item, index, array) => {
					if (index !== array.length - 1) {
						return v('li', { classes: [c.breadcrumb_item] }, [
							w(
								Link,
								{
									to: 'view-project-group',
									params: {
										owner: project.createUserName,
										project: project.name,
										parentPath: item.path
									}
								},
								[`${item.name}`]
							)
						]);
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

	private _renderNewResourceButtonGroup() {
		const disabled = !this._isAuthenticated();
		const { messages } = this._localizedMessages;
		return v('div', { classes: [c.btn_group, c.btn_group_sm, c.mr_2], role: 'group' }, [
			// 这里没有设置 params，却依然起作用，因为当前页面的 url 中已包含 {owner}/{project}
			w(Link, { classes: [c.btn, c.btn_outline_secondary], to: 'new-page', disabled }, [`${messages.newPage}`]),
			w(Link, { classes: [c.btn, c.btn_outline_secondary], to: 'new-group', disabled }, [`${messages.newGroup}`])
		]);
	}

	private _renderTable() {
		return v('div', { classes: [c.card] }, [this._renderLatestCommitInfo(), this._renderResources()]);
	}

	/**
	 *  最近提交信息区
	 */
	private _renderLatestCommitInfo() {
		const { latestCommitInfo } = this.properties;
		if (!latestCommitInfo) {
			return;
		}
		const { messages } = this._localizedMessages;
		return v('div', { classes: [c.card_header, c.text_muted, c.px_2, c.border_bottom_0, css.recentCommit] }, [
			// 最近提交的用户信息
			w(Link, { to: 'profile', params: { user: latestCommitInfo.userName }, classes: [c.mr_2] }, [
				v('img', {
					width: 20,
					height: 20,
					classes: [c.avatar, c.mr_1],
					src: `${latestCommitInfo.avatarUrl}`
				}),
				`${latestCommitInfo.userName}`
			]),
			// 最近提交说明
			v('span', [`${latestCommitInfo.shortMessage}`]),
			' ',
			// 最近提交时间
			v('span', { classes: [c.float_right] }, [
				`${messages.latestCommitLabel}`,
				w(Moment, { datetime: latestCommitInfo.commitTime })
			])
		]);
	}

	private _renderResources() {
		const { projectResources } = this.properties;

		return projectResources
			? v('table', { classes: [c.table, c.table_hover, c.mb_0] }, [
					v('tbody', projectResources.map((resource) => this._renderTr(resource)))
			  ])
			: w(Spinner, {});
	}

	private _renderTr(projectResource: ProjectResource) {
		// gitStatus 为 undefined 时，表示文件内容未变化。
		// 未变化
		// 未跟踪
		// 已修改
		const { gitStatus, resourceType } = projectResource;
		const { project, parentPath } = this.properties;

		let to = '';
		let params: Params = {};
		let untracked = false;
		let statusLetter = '';
		let statusColor = '';
		let statusTooltip = '';

		if (resourceType === ResourceType.Group) {
			to = 'view-project-group';
			const fullPath = parentPath === '' ? projectResource.key : parentPath + '/' + projectResource.key;
			params = { owner: project.createUserName, project: project.name, parentPath: fullPath };

			if (gitStatus === GitFileStatus.Untracked) {
				untracked = true;
				statusColor = c.text_muted;
			} else {
				statusLetter = '●';
			}
		} else {
			if (gitStatus === GitFileStatus.Untracked || gitStatus === GitFileStatus.Added) {
				untracked = true;
				statusLetter = 'U';
				statusColor = c.text_success;
				statusTooltip = '未跟踪';
			} else if (gitStatus === GitFileStatus.Modified) {
				statusLetter = 'M';
				statusColor = c.text_warning;
				statusTooltip = '已修改';
			}
		}

		return v('tr', [
			// 图标
			v('td', { classes: [css.icon] }, [
				w(FontAwesomeIcon, {
					icon: projectResource.icon.split(' ') as [IconPrefix, IconName],
					title: projectResource.title
				})
			]),
			// 资源名称
			v('td', { classes: [css.content, c.px_1] }, [
				v('span', { classes: [css.truncate] }, [
					w(Link, { to, params, title: `${projectResource.name}`, classes: [statusColor] }, [
						`${projectResource.name}`
					])
				])
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`]),
			// 最近提交信息
			v('td', { classes: [css.message, c.text_muted] }, [
				untracked
					? undefined
					: v('span', { classes: [css.truncate] }, [
							v('a', { title: `${projectResource.latestFullMessage}` }, [
								`${projectResource.latestShortMessage}`
							])
					  ])
			]),
			// 最近提交时间
			v('td', { classes: [css.age, c.text_muted] }, [
				// 使用 moment.js 进行格式化
				untracked
					? undefined
					: v('span', { classes: [css.truncate] }, [
							w(Moment, { datetime: `${projectResource.latestCommitTime}` })
					  ])
			])
		]);
	}
}
