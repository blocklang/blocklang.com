import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';

import { v, w } from '@dojo/framework/core/vdom';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import { Project, ProjectResource, CommitInfo, DeployInfo, UncommittedFile, WithTarget } from '../../interfaces';
import Moment from '../../widgets/moment';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import { IconName, IconPrefix } from '@fortawesome/fontawesome-svg-core';
import MarkdownPreview from '../../widgets/markdown-preview';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './ViewProject.m.css';
import {
	ProjectPathPayload,
	UnstagedChangesPayload,
	StagedChangesPayload,
	CommitMessagePayload,
} from '../../processes/interfaces';

import Spinner from '../../widgets/spinner';
import ProjectHeader from '../widgets/ProjectHeader';
import { isEmpty } from '../../util';
import Exception from '../error/Exception';
import { ResourceType, GitFileStatus, ValidateStatus } from '../../constant';
import { Params } from '@dojo/framework/routing/interfaces';
import watch from '@dojo/framework/core/decorators/watch';
import { canCommit } from '../../permission';
import LatestCommitInfo from './widgets/LatestCommitInfo';

export interface ViewProjectProperties {
	loggedUsername: string;
	project: Project;
	groupId: number; // 根分组的 id，默认是 -1
	path: string; // 根分组的 path，默认为空字符串
	childResources: ProjectResource[];
	latestCommitInfo: CommitInfo;
	readme?: string;
	userDeployInfo: DeployInfo;
	releaseCount: number;
	stagedChanges: UncommittedFile[];
	unstagedChanges: UncommittedFile[];

	// validation
	commitMessageValidateStatus?: ValidateStatus;
	commitMessageErrorMessage?: string;
	commitMessage?: string;

	onGetDeployInfo: (opt: ProjectPathPayload) => void;
	onStageChanges: (opt: StagedChangesPayload) => void;
	onUnstageChanges: (opt: UnstagedChangesPayload) => void;
	onCommitMessageInput: (opt: CommitMessagePayload) => void;
	onCommit: (opt: ProjectPathPayload) => void;
}

enum ViewStatus {
	Edit,
	Commit,
}

@theme(css)
export default class ViewProject extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	@watch()
	private _viewStatus: ViewStatus = ViewStatus.Edit;

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
			this._viewStatus === ViewStatus.Edit ? this._renderEditView() : this._renderCommitView(),
		]);
	}

	private _renderEditView() {
		return v('div', [this._renderTable(), this._renderReadme()]);
	}

	private _renderCommitView() {
		const { messages } = this._localizedMessages;
		const {
			stagedChanges = [],
			unstagedChanges = [],
			commitMessage = '',
			commitMessageValidateStatus = ValidateStatus.UNVALIDATED,
			commitMessageErrorMessage,
		} = this.properties;

		const inputClasses = [c.form_control];
		if (commitMessageValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}

		// 1. 提示信息不能为空
		// 2. stagedChanges.length > 0
		const disabled =
			commitMessageValidateStatus === ValidateStatus.VALID && stagedChanges.length > 0 ? false : true;

		return v('div', [
			v('hr'),
			// staged changes
			v('div', { classes: [c.mt_2] }, [
				v('div', { classes: [c.d_flex, c.justify_content_between, c.align_items_center] }, [
					v('strong', [`${messages.stagedChangesLabel}`]),
					v('span', { classes: [c.badge, c.badge_secondary] }, [`${stagedChanges.length}`]),
				]),
				v('table', { classes: [c.table, c.table_hover, c.table_borderless, c.mt_1] }, [
					v(
						'tbody',
						stagedChanges.map((item) =>
							w(StagedChangesItem, {
								uncommittedFile: item,
								onUnstageChanges: this._onUnstageChanges,
							})
						)
					),
				]),
			]),
			// unstaged changes
			v('div', { classes: [c.mt_2] }, [
				v('div', { classes: [c.d_flex, c.justify_content_between, c.align_items_center] }, [
					v('strong', [`${messages.unstagedChangesLabel}`]),
					v('span', { classes: [c.badge, c.badge_secondary] }, [`${unstagedChanges.length}`]),
				]),
				v('table', { classes: [c.table, c.table_hover, c.table_borderless, c.mt_1] }, [
					v(
						'tbody',
						unstagedChanges.map((item) =>
							w(UnstagedChangesItem, {
								uncommittedFile: item,
								onStageChanges: this._onStageChanges,
							})
						)
					),
				]),
			]),
			v('form', { classes: [c.needs_validation, c.mb_3], novalidate: 'novalidate' }, [
				v('div', { classes: [c.form_group, c.position_relative] }, [
					v('textarea', {
						classes: inputClasses,
						rows: 3,
						placeholder: `${messages.commitMessageTip}${messages.requiredLabel}`,
						oninput: this._onCommitMessageInput,
						value: `${commitMessage}`,
					}),
					commitMessageValidateStatus === ValidateStatus.INVALID
						? v('div', { classes: [c.invalid_tooltip], innerHTML: `${commitMessageErrorMessage}` })
						: undefined,
				]),
				v(
					'button',
					{
						type: 'button',
						classes: [c.btn, c.btn_primary],
						disabled,
						onclick: disabled ? undefined : this._onCommit,
					},
					[`${messages.commitLabel}`]
				),
				v('small', { classes: [c.text_muted, c.ml_2] }, [`${messages.commitHelp}`]),
			]),
		]);
	}

	private _onCommitMessageInput({ target: { value: commitMessage } }: WithTarget) {
		this.properties.onCommitMessageInput({ commitMessage });
	}

	private _onCommit() {
		const { project } = this.properties;
		this.properties.onCommit({ owner: project.createUserName, project: project.name });
	}

	private _onUnstageChanges(fullKeyPath: string) {
		const { project } = this.properties;

		this.properties.onUnstageChanges({
			owner: project.createUserName,
			project: project.name,
			files: [fullKeyPath],
		});
	}

	private _onStageChanges(fullKeyPath: string) {
		const { project } = this.properties;
		this.properties.onStageChanges({ owner: project.createUserName, project: project.name, files: [fullKeyPath] });
	}

	private _isNotFound() {
		const { project } = this.properties;
		return isEmpty(project);
	}

	private _isLogined() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle },
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderNavigation() {
		return v('div', { classes: [c.d_flex, c.justify_content_between, c.mb_2] }, [
			v('div', [this._renderCommitButtonGroup()]),
			v('div', { classes: [] }, [
				this._viewStatus === ViewStatus.Edit ? this._renderCreateProjectButtonGroup() : undefined,
			]),
		]);
	}

	private _renderCommitButtonGroup() {
		if (!this._isLogined()) {
			return;
		}

		const { project } = this.properties;
		if (!canCommit(project.accessLevel)) {
			return;
		}

		const { messages } = this._localizedMessages;
		const { unstagedChanges = [], stagedChanges = [] } = this.properties;

		const changeCount = unstagedChanges.length + stagedChanges.length;

		return v(
			'div',
			{
				classes: [c.btn_group, c.btn_group_sm],
				role: 'group',
			},
			[
				v(
					'button',
					{
						type: 'button',
						classes: [
							c.btn,
							c.btn_outline_primary,
							this._viewStatus === ViewStatus.Edit ? c.active : undefined,
						],
						onclick: this._switchToEditMode,
					},
					[w(FontAwesomeIcon, { icon: 'copy' }), ` ${messages.editLabel}`]
				),
				v(
					'button',
					{
						type: 'button',
						classes: [
							c.btn,
							c.btn_outline_primary,
							this._viewStatus === ViewStatus.Commit ? c.active : undefined,
						],
						onclick: this._switchToCommitMode,
					},
					[
						w(FontAwesomeIcon, { icon: 'code-branch' }),
						` ${messages.commitLabel} `,
						changeCount > 0
							? v('span', { classes: [c.badge, c.badge_light] }, [`${changeCount}`])
							: undefined,
					]
				),
			]
		);
	}

	private _switchToEditMode() {
		if (this._viewStatus === ViewStatus.Commit) {
			this._viewStatus = ViewStatus.Edit;
		}
	}

	private _switchToCommitMode() {
		if (this._viewStatus === ViewStatus.Edit) {
			this._viewStatus = ViewStatus.Commit;
		}
	}

	// 如果没有 write 权限，则让新建按钮失效
	private _renderCreateProjectButtonGroup() {
		const { project } = this.properties;
		let disabled = false;
		if (!this._isLogined()) {
			disabled = true;
		} else {
			if (!canCommit(project.accessLevel)) {
				disabled = true;
			}
		}
		const { messages } = this._localizedMessages;

		return v(
			'div',
			{ classes: [c.btn_group, c.btn_group_sm], role: 'group' },
			disabled
				? [
						v(
							'a',
							{
								key: 'web',
								classes: [c.btn, c.btn_outline_secondary, c.disabled],
								tabIndex: -1,
								'aria-disabled': 'true',
							},
							[`${messages.createWebProject}`]
						),
						v(
							'a',
							{
								key: 'mini-program',
								classes: [c.btn, c.btn_outline_secondary, c.disabled],
								tabIndex: -1,
								'aria-disabled': 'true',
							},
							[`${messages.createMiniProgram}`]
						),
				  ]
				: [
						w(
							Link,
							{
								key: 'web',
								classes: [c.btn, c.btn_outline_secondary],
								to: 'new-project',
								params: { owner: project.createUserName, repository: project.name, type: 'web' },
							},
							[`${messages.createWebProject}`]
						),
						w(
							Link,
							{
								key: 'mini-program',
								classes: [c.btn, c.btn_outline_secondary],
								to: 'new-project',
								params: {
									owner: project.createUserName,
									repository: project.name,
									type: 'miniprogram',
								},
							},
							[`${messages.createMiniProgram}`]
						),
				  ]
		);
	}

	private _renderTable() {
		const { latestCommitInfo } = this.properties;

		return v('div', { classes: [c.card] }, [
			w(LatestCommitInfo, { latestCommitInfo }), // 最近提交信息区
			this._renderResources(),
		]);
	}

	private _renderResources() {
		const { childResources } = this.properties;

		return childResources
			? v('table', { classes: [c.table, c.table_hover, c.mb_0] }, [
					v(
						'tbody',
						childResources.map((resource) => this._renderTr(resource))
					),
			  ])
			: w(Spinner, {});
	}

	private _renderTr(projectResource: ProjectResource) {
		// gitStatus 为 undefined 时，表示文件内容未变化。
		// 未变化
		// 未跟踪
		// 已修改
		const { project, path } = this.properties;
		return w(ProjectResourceRow, { projectResource, project, parentPath: path });
	}

	private _renderReadme() {
		const projectFile = { name: 'README.md' };
		const canEditProject = false;

		const { readme = '' } = this.properties;

		return v('div', { classes: [c.card, c.mt_3] }, [
			v('div', { classes: [c.card_header, c.px_2] }, [
				v('div', {}, [w(FontAwesomeIcon, { icon: 'book-open' }), ` ${projectFile.name}`]),
				canEditProject ? w(Link, { to: '' }, [w(FontAwesomeIcon, { icon: 'edit' })]) : null,
			]),

			v('div', { classes: [c.card_body, c.markdown_body] }, [w(MarkdownPreview, { value: readme })]),
		]);
	}
}

interface ProjectResourceRowProperties {
	project: Project;
	projectResource: ProjectResource;
	parentPath: string;
}

@theme(css)
class ProjectResourceRow extends ThemedMixin(I18nMixin(WidgetBase))<ProjectResourceRowProperties> {
	protected render() {
		const { projectResource } = this.properties;
		const { gitStatus, resourceType } = projectResource;

		let showCommitInfo = true;
		if (!projectResource.latestShortMessage) {
			showCommitInfo = false;
		}

		let statusLetter = '';
		let statusColor = '';
		let statusTooltip = '';

		if (resourceType === ResourceType.Group) {
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
		} else {
			if (gitStatus === GitFileStatus.Untracked || gitStatus === GitFileStatus.Added) {
				statusLetter = 'U';
				statusColor = c.text_success;
				statusTooltip = '未跟踪';
			} else if (gitStatus === GitFileStatus.Modified || gitStatus === GitFileStatus.Changed) {
				statusLetter = 'M';
				statusColor = c.text_warning;
				statusTooltip = '已修改';
			}
		}

		const to = this._getUrl();
		const params: Params = this._getParams();

		let title = projectResource.name;
		if (statusTooltip !== '') {
			title = title + ' • ' + statusTooltip;
		}

		return v('tr', [
			// 图标
			v('td', { classes: [css.icon] }, [
				w(FontAwesomeIcon, {
					icon: projectResource.icon.split(' ') as [IconPrefix, IconName],
					title: projectResource.title,
				}),
			]),
			// 资源名称
			v('td', { classes: [css.content, c.px_1] }, [
				v('span', { classes: [css.truncate] }, [
					w(Link, { to, params, title, classes: [statusColor] }, [`${projectResource.name}`]),
				]),
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`]),
			// 最近提交信息
			v('td', { classes: [css.message, c.text_muted] }, [
				showCommitInfo
					? v('span', { classes: [css.truncate] }, [
							v('a', { title: `${projectResource.latestFullMessage}` }, [
								`${projectResource.latestShortMessage}`,
							]),
					  ])
					: undefined,
			]),
			// 最近提交时间
			v('td', { classes: [css.age, c.text_muted] }, [
				// 使用 moment.js 进行格式化
				showCommitInfo
					? v('span', { classes: [css.truncate] }, [
							w(Moment, { datetime: `${projectResource.latestCommitTime}` }),
					  ])
					: undefined,
			]),
		]);
	}

	private _getUrl(): string {
		const { projectResource } = this.properties;
		const { resourceType, key } = projectResource;

		if (resourceType === ResourceType.Group || resourceType === ResourceType.Project) {
			return 'view-project-group';
		}

		if (resourceType === ResourceType.Page) {
			return 'view-project-page';
		}

		if (resourceType === ResourceType.PageTemplet) {
			return 'view-project-templet';
		}

		if (resourceType === ResourceType.Service) {
			return 'view-project-service';
		}

		if (resourceType === ResourceType.Dependence) {
			return 'view-project-dependence';
		}

		if (resourceType === ResourceType.File) {
			if (key === 'README') {
				return 'view-project-readme';
			}
		}

		return '';
	}

	private _getParams(): Params {
		const { project, parentPath, projectResource } = this.properties;
		const { resourceType } = projectResource;

		if (resourceType === ResourceType.Group || resourceType === ResourceType.Project) {
			const fullPath = parentPath === '' ? projectResource.key : parentPath + '/' + projectResource.key;
			return { owner: project.createUserName, project: project.name, parentPath: fullPath };
		}

		const fullPath = parentPath === '' ? projectResource.key : parentPath + '/' + projectResource.key;
		return { owner: project.createUserName, project: project.name, path: fullPath };
	}
}

export interface StagedChangeProperties {
	uncommittedFile: UncommittedFile;
	onUnstageChanges: (fullKeyPath: string) => void;
}

class StagedChangesItem extends ThemedMixin(I18nMixin(WidgetBase))<StagedChangeProperties> {
	protected render() {
		const { uncommittedFile } = this.properties;

		let statusLetter = '';
		let statusColor = '';
		let statusTooltip = '';

		const gitStatus = uncommittedFile.gitStatus;

		if (gitStatus === GitFileStatus.Added) {
			statusLetter = 'A';
			statusColor = c.text_success;
			statusTooltip = '已跟踪';
		} else if (gitStatus === GitFileStatus.Changed) {
			statusLetter = 'M';
			statusColor = c.text_warning;
			statusTooltip = '已修改';
		} else if (gitStatus === GitFileStatus.Removed) {
			statusLetter = 'D';
			statusColor = c.text_danger;
			statusTooltip = '已删除';
		}

		return v('tr', [
			v('td', { classes: [css.icon] }, [
				w(FontAwesomeIcon, {
					icon: uncommittedFile.icon.split(' ') as [IconPrefix, IconName],
					title: uncommittedFile.iconTitle,
				}),
			]),
			// 资源名称
			v('td', { classes: [c.px_1] }, [
				v('div', { classes: [css.truncate] }, [
					gitStatus === GitFileStatus.Removed
						? v('del', [`${uncommittedFile.resourceName}`])
						: v('span', [`${uncommittedFile.resourceName}`]),
					v('small', { classes: [c.text_muted, c.ml_1] }, [`${uncommittedFile.parentNamePath}`]),
				]),
			]),
			v('td', { classes: [css.operator] }, [
				v(
					'span',
					{
						onclick: this._onUnstageChanges,
					},
					[
						w(FontAwesomeIcon, {
							icon: 'minus',
							title: '撤销暂存的更改',
							classes: [css.op],
						}),
					]
				),
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`]),
		]);
	}

	private _onUnstageChanges() {
		const { onUnstageChanges, uncommittedFile } = this.properties;

		onUnstageChanges(uncommittedFile.fullKeyPath);
	}
}

export interface UnstagedChangeProperties {
	uncommittedFile: UncommittedFile;
	onStageChanges: (fullKeyPath: string) => void;
}

class UnstagedChangesItem extends ThemedMixin(I18nMixin(WidgetBase))<UnstagedChangeProperties> {
	protected render() {
		const { uncommittedFile } = this.properties;

		const { gitStatus } = uncommittedFile;

		let statusLetter = '';
		let statusColor = '';
		let statusTooltip = '';

		if (gitStatus === GitFileStatus.Untracked) {
			statusLetter = 'U';
			statusColor = c.text_success;
			statusTooltip = '未跟踪';
		} else if (gitStatus === GitFileStatus.Modified) {
			statusLetter = 'M';
			statusColor = c.text_warning;
			statusTooltip = '已修改';
		} else if (gitStatus === GitFileStatus.Deleted) {
			statusLetter = 'D';
			statusColor = c.text_danger;
			statusTooltip = '已删除';
		}

		return v('tr', [
			v('td', { classes: [css.icon] }, [
				w(FontAwesomeIcon, {
					icon: uncommittedFile.icon.split(' ') as [IconPrefix, IconName],
					title: uncommittedFile.iconTitle,
				}),
			]),
			// 资源名称
			v('td', { classes: [c.px_1] }, [
				v('div', { classes: [css.truncate] }, [
					gitStatus === GitFileStatus.Deleted
						? v('del', [`${uncommittedFile.resourceName}`])
						: v('span', [`${uncommittedFile.resourceName}`]),
					v('small', { classes: [c.text_muted, c.ml_1] }, [`${uncommittedFile.parentNamePath}`]),
				]),
			]),
			v('td', { classes: [css.operator] }, [
				v(
					'span',
					{
						onclick: this._onStageChanges,
					},
					[
						w(FontAwesomeIcon, {
							icon: 'plus',
							title: '暂存更改',
							classes: [css.op],
						}),
					]
				),
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`]),
		]);
	}

	private _onStageChanges() {
		const { onStageChanges, uncommittedFile } = this.properties;

		onStageChanges(uncommittedFile.fullKeyPath);
	}
}
