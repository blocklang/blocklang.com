import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import { v, w } from '@dojo/framework/widget-core/d';
import global from '@dojo/framework/shim/global';

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
import { ProjectResourcePathPayload } from '../../processes/interfaces';
import GoToParentGroupLink from './widgets/GoToParentGroupLink';
import LatestCommitInfo from './widgets/LatestCommitInfo';
import ProjectResourceBreadcrumb from './widgets/ProjectResourceBreadcrumb';

export interface ViewProjectGroupProperties {
	loggedUsername: string;
	project: Project;
	groupId: number; // 当前分组 id
	path: string; // 路径，从根分组到当前分组，使用 / 分割
	groups: ProjectGroup[]; // 分组列表，从根分组到当前分组
	childResources: ProjectResource[]; // 当前分组下的所有子资源
	latestCommitInfo: CommitInfo; // 当前分组的最近一次提交信息
	onOpenGroup: (opt: ProjectResourcePathPayload) => void;
}

@theme(css)
export default class ViewProjectGroup extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectGroupProperties> {
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
			this._renderTable(),
			this._renderNoResourceAlert()
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
		const { project, groups, onOpenGroup } = this.properties;

		return v('div', { classes: [c.d_flex, c.justify_content_between, c.mb_2] }, [
			v('div', {}, [w(ProjectResourceBreadcrumb, { project, pathes: groups, onOpenGroup })]),
			v('div', { classes: [] }, [this._renderNewResourceButtonGroup()])
		]);
	}

	private _renderNewResourceButtonGroup() {
		const disabled = !this._isAuthenticated();
		const { messages } = this._localizedMessages;
		const { project, path } = this.properties;

		return v('div', { classes: [c.btn_group, c.btn_group_sm, c.mr_2], role: 'group' }, [
			w(
				Link,
				{
					classes: [c.btn, c.btn_outline_secondary],
					to: 'new-page',
					params: { owner: project.createUserName, project: project.name, path },
					onClick: (event: MouseEvent) => {
						console.log(event);
					},
					disabled
				},
				[`${messages.newPage}`]
			),
			w(
				Link,
				{
					classes: [c.btn, c.btn_outline_secondary],
					to: 'new-group',
					params: { owner: project.createUserName, project: project.name, path },
					onClick: (event: MouseEvent) => {
						console.log(event);
					},
					disabled
				},
				[`${messages.newGroup}`]
			)
		]);
	}

	private _renderTable() {
		const { latestCommitInfo } = this.properties;

		return v('div', { classes: [c.card, !latestCommitInfo ? c.border_top_0 : undefined] }, [
			w(LatestCommitInfo, { latestCommitInfo }), // 最近提交信息区
			this._renderResources()
		]);
	}

	private _renderResources() {
		const { childResources } = this.properties;

		return childResources
			? v('table', { classes: [c.table, c.table_hover, c.mb_0] }, [
					v('tbody', [this._renderBackTr(), ...childResources.map((resource) => this._renderTr(resource))])
			  ])
			: w(Spinner, {});
	}

	private _renderBackTr() {
		const { project, groups = [] } = this.properties;

		if (groups.length === 0) {
			return;
		}

		return v('tr', [
			v('td', { classes: [css.icon] }, []),
			v('td', { colspan: '4', classes: [c.pl_1] }, [
				w(GoToParentGroupLink, { project, parentGroups: groups, onGoToGroup: this._onGoToGroup })
			])
		]);
	}

	private _onGoToGroup(opt: ProjectResourcePathPayload) {
		this.properties.onOpenGroup(opt);
	}

	private _renderTr(projectResource: ProjectResource) {
		const { project, path } = this.properties;
		return w(ProjectResourceRow, { projectResource, project, parentPath: path, onOpenGroup: this._onOpenGroup });
	}

	private _onOpenGroup(opt: ProjectResourcePathPayload) {
		this.properties.onOpenGroup(opt);
	}

	private _renderNoResourceAlert() {
		const { childResources } = this.properties;
		if (childResources.length === 0) {
			return v('div', { classes: [c.alert, c.alert_info, c.text_center, c.mt_3], role: 'alert' }, [
				'此分组下无内容'
			]);
		}
	}
}

interface ProjectResourceRowProperties {
	project: Project;
	parentPath: string; // 在这里取名为 parentPath 是合适的，因为这里是相对于页面等资源，就是获取父分组的路径信息
	projectResource: ProjectResource;
	onOpenGroup: (opt: ProjectResourcePathPayload) => void;
}

@theme(css)
class ProjectResourceRow extends ThemedMixin(I18nMixin(WidgetBase))<ProjectResourceRowProperties> {
	protected render() {
		// gitStatus 为 undefined 时，表示文件内容未变化。
		// 未变化
		// 未跟踪
		// 已修改
		const { project, parentPath, projectResource } = this.properties;
		const { gitStatus, resourceType } = projectResource;

		let showCommitInfo = true;
		if (!projectResource.latestShortMessage) {
			showCommitInfo = false;
		}

		let statusLetter = '';
		let statusColor = '';
		let statusTooltip = '';
		let fullPath;
		let isGroup = false;
		if (resourceType === ResourceType.Group) {
			fullPath = parentPath === '' ? projectResource.key : parentPath + '/' + projectResource.key;

			if (gitStatus === GitFileStatus.Untracked || gitStatus === GitFileStatus.Added) {
				statusLetter = '●';
				statusColor = c.text_success;
				statusTooltip = '包含变更的内容';
			} else if (gitStatus === GitFileStatus.Modified || gitStatus === GitFileStatus.Changed) {
				// 如果目录中同时有新增和修改，则显示修改颜色
				// 如果目录中只有新增，则显示新增颜色
				statusLetter = '●';
				statusColor = c.text_warning;
				statusTooltip = '包含变更的内容';
			} else {
				statusColor = c.text_muted;
			}

			isGroup = true;
		} else {
			if (gitStatus === GitFileStatus.Untracked || gitStatus === GitFileStatus.Added) {
				statusLetter = 'U';
				statusColor = c.text_success;
				statusTooltip = '未跟踪';
			} else if (gitStatus === GitFileStatus.Modified) {
				statusLetter = 'M';
				statusColor = c.text_warning;
				statusTooltip = '已修改';
			}

			isGroup = false;
		}

		let title = projectResource.name;
		if (statusTooltip !== '') {
			title = title + ' • ' + statusTooltip;
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
					isGroup
						? v(
								'a',
								{
									classes: [statusColor],
									href: `/${project.createUserName}/${project.name}/groups/${fullPath}`,
									title,
									// 因为 dojo 5.0 的 route 不支持通配符，这里尝试实现类似效果
									onclick: this._onOpenGroup
								},
								[`${projectResource.name}`]
						  )
						: w(Link, { to: '', title, classes: [statusColor] }, [`${projectResource.name}`])
				])
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`]),
			// 最近提交信息
			v('td', { classes: [css.message, c.text_muted] }, [
				showCommitInfo
					? v('span', { classes: [css.truncate] }, [
							v('a', { title: `${projectResource.latestFullMessage}` }, [
								`${projectResource.latestShortMessage}`
							])
					  ])
					: undefined
			]),
			// 最近提交时间
			v('td', { classes: [css.age, c.text_muted] }, [
				// 使用 moment.js 进行格式化
				showCommitInfo
					? v('span', { classes: [css.truncate] }, [
							w(Moment, { datetime: `${projectResource.latestCommitTime}` })
					  ])
					: undefined
			])
		]);
	}

	private _onOpenGroup(event: any) {
		const { project, projectResource, parentPath } = this.properties;
		event.stopPropagation();
		event.preventDefault();
		const fullPath = parentPath === '' ? projectResource.key : parentPath + '/' + projectResource.key;
		this.properties.onOpenGroup({ owner: project.createUserName, project: project.name, parentPath: fullPath });
		global.window.history.pushState({}, '', `/${project.createUserName}/${project.name}/groups/${fullPath}`);
		return false;
	}
}
