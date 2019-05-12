import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import { v, w, dom } from '@dojo/framework/widget-core/d';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import { Project, ProjectResource, CommitInfo, DeployInfo, UncommittedFile, WithTarget } from '../../interfaces';
import Moment from '../../widgets/moment';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import { IconName, IconPrefix } from '@fortawesome/fontawesome-svg-core';
import MarkdownPreview from '../../widgets/markdown-preview';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';

import * as c from '../../className';
import * as css from './ViewProject.m.css';
import {
	ProjectPathPayload,
	UnstagedChangesPayload,
	StagedChangesPayload,
	CommitMessagePayload
} from '../../processes/interfaces';

import * as $ from 'jquery';
import Spinner from '../../widgets/spinner';
import ProjectHeader from '../widgets/ProjectHeader';
import { isEmpty } from '../../util';
import Exception from '../error/Exception';
import { ResourceType, GitFileStatus, ValidateStatus } from '../../constant';
import { Params } from '@dojo/framework/routing/interfaces';
import watch from '@dojo/framework/widget-core/decorators/watch';

export interface ViewProjectProperties {
	loggedUsername: string;
	project: Project;
	parentPath: string;
	parentId: number;
	projectResources: ProjectResource[];
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
	Commit
}

@theme(css)
export default class ViewProject extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	@watch()
	private _viewStatus: ViewStatus = ViewStatus.Edit;

	protected render() {
		if (this._isNotFound()) {
			return w(Exception, { type: '404' });
		}

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderNavigation(),
			this._viewStatus === ViewStatus.Edit ? this._renderEditView() : this._renderCommitView()
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
			commitMessageErrorMessage
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
				v('div', { classes: [c.d_flex, c.justify_content_between] }, [
					v('strong', [`${messages.stagedChangesLabel}`]),
					v('span', { classes: [c.badge, c.badge_secondary] }, [`${stagedChanges.length}`])
				]),
				v('table', { classes: [c.table, c.table_hover, c.table_borderless, c.mt_1] }, [
					v(
						'tbody',
						stagedChanges.map((item) =>
							w(StagedChangesItem, {
								uncommittedFile: item,
								onUnstageChanges: this._onUnstageChanges
							})
						)
					)
				])
			]),
			// unstaged changes
			v('div', { classes: [c.mt_2] }, [
				v('div', { classes: [c.d_flex, c.justify_content_between] }, [
					v('strong', [`${messages.unstagedChangesLabel}`]),
					v('span', { classes: [c.badge, c.badge_secondary] }, [`${unstagedChanges.length}`])
				]),
				v('table', { classes: [c.table, c.table_hover, c.table_borderless, c.mt_1] }, [
					v(
						'tbody',
						unstagedChanges.map((item) =>
							w(UnstagedChangesItem, {
								uncommittedFile: item,
								onStageChanges: this._onStageChanges
							})
						)
					)
				])
			]),
			v('form', { classes: [c.needs_validation, c.mb_3], novalidate: 'novalidate' }, [
				v('div', { classes: [c.form_group, c.position_relative] }, [
					v('textarea', {
						classes: inputClasses,
						rows: 3,
						placeholder: `${messages.commitMessageTip}${messages.requiredLabel}`,
						oninput: this._onCommitMessageInput,
						value: `${commitMessage}`
					}),
					commitMessageValidateStatus === ValidateStatus.INVALID
						? v('div', { classes: [c.invalid_tooltip], innerHTML: `${commitMessageErrorMessage}` })
						: undefined
				]),
				v(
					'button',
					{
						type: 'button',
						classes: [c.btn, c.btn_primary],
						disabled,
						onclick: disabled ? undefined : this._onCommit
					},
					[`${messages.commitLabel}`]
				),
				v('small', { classes: [c.text_muted, c.ml_2] }, [`${messages.commitHelp}`])
			])
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
			files: [fullKeyPath]
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
			v('div', [this._renderCommitButtonGroup()]),
			v('div', { classes: [] }, [
				this._viewStatus === ViewStatus.Edit ? this._renderNewResourceButtonGroup() : undefined,
				// 发布按钮，后面显示发布次数
				this._renderReleaseButton(),
				// 部署按钮，显示部署步骤
				this._renderDeployButton()
			])
		]);
	}

	private _renderCommitButtonGroup() {
		const authed = this._isAuthenticated();
		if (!authed) {
			return;
		}

		const { messages } = this._localizedMessages;
		const { unstagedChanges = [], stagedChanges = [] } = this.properties;

		const changeCount = unstagedChanges.length + stagedChanges.length;

		return v(
			'div',
			{
				classes: [c.btn_group, c.btn_group_sm],
				role: 'group'
			},
			[
				v(
					'button',
					{
						type: 'button',
						classes: [
							c.btn,
							c.btn_outline_primary,
							this._viewStatus === ViewStatus.Edit ? c.active : undefined
						],
						onclick: this._switchToEditMode
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
							this._viewStatus === ViewStatus.Commit ? c.active : undefined
						],
						onclick: this._switchToCommitMode
					},
					[
						w(FontAwesomeIcon, { icon: 'code-branch' }),
						` ${messages.commitLabel} `,
						changeCount > 0
							? v('span', { classes: [c.badge, c.badge_light] }, [`${changeCount}`])
							: undefined
					]
				)
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

	private _renderNewResourceButtonGroup() {
		const disabled = !this._isAuthenticated();
		const { messages } = this._localizedMessages;
		const { project } = this.properties;

		return v('div', { classes: [c.btn_group, c.btn_group_sm, c.mr_2], role: 'group' }, [
			w(
				Link,
				{
					classes: [c.btn, c.btn_outline_secondary],
					to: 'new-page-root',
					params: { owner: project.createUserName, project: project.name },
					disabled
				},
				[`${messages.newPage}`]
			),
			w(
				Link,
				{
					classes: [c.btn, c.btn_outline_secondary],
					to: 'new-group-root',
					params: { owner: project.createUserName, project: project.name },
					disabled
				},
				[`${messages.newGroup}`]
			)
		]);
	}

	private _renderReleaseButton() {
		const { messages } = this._localizedMessages;
		const { releaseCount = 0 } = this.properties;

		// 这里没有设置 params，却依然起作用，因为当前页面的 url 中已包含 {owner}/{project}
		return w(Link, { to: 'list-release', classes: [c.btn, c.btn_outline_secondary, c.btn_sm, c.mr_2] }, [
			`${messages.releaseLabel} `,
			v('span', { classes: [c.badge, c.badge_light] }, [`${releaseCount}`])
		]);
	}

	/**
	 * 渲染部署按钮
	 */
	private _renderDeployButton() {
		const { messages } = this._localizedMessages;
		const isAuth: boolean = this._isAuthenticated();

		if (isAuth) {
			return v('div', { classes: [c.btn_group] }, [
				v(
					'button',
					{
						classes: [c.btn, c.btn_outline_secondary, c.dropdown_toggle, c.btn_sm],
						type: 'button',
						'data-toggle': 'dropdown',
						'aria-haspopup': 'true',
						'aria-expanded': 'false',
						id: 'dropdownDeployButton',
						onclick: this._onDeployButtonClick
					},
					[`${messages.deployLabel}`]
				),

				this._renderDeployDropdownMenu()
			]);
		} else {
			const node = document.createElement('span');
			const vnode = dom(
				{
					node,
					props: {
						classes: [c.d_inline_block],
						tabIndex: 0,
						title: messages.deployNotLoginTip
					},
					attrs: {
						'data-toggle': 'tooltip'
					},
					onAttach: () => {
						// 使用 dom 函数，就是为了调用 tooltip 方法
						// 因为 jQuery 的 $ 中没有 bootstrap 的 tooltip 方法
						// 因为 bootstrap 默认没有初始化 tooltip
						($(node) as any).tooltip();
					}
				},
				[
					v(
						'button',
						{
							classes: [c.btn, c.btn_outline_secondary, c.dropdown_toggle, c.btn_sm],
							type: 'button',
							styles: { pointerEvents: 'none' },
							disabled: true
						},
						[`${messages.deployLabel}`]
					)
				]
			);
			return v('div', { classes: [c.btn_group, c.ml_2] }, [vnode]);
		}
	}

	private _onDeployButtonClick() {
		const { project } = this.properties;
		this.properties.onGetDeployInfo &&
			this.properties.onGetDeployInfo({ owner: project.createUserName, project: project.name });
	}

	private _renderDeployDropdownMenu() {
		const { userDeployInfo } = this.properties;

		return v(
			'div',
			{
				classes: [c.dropdown_menu, c.dropdown_menu_right, c.p_2, css.deployMenu],
				'aria-labelledby': 'dropdownDeployButton',
				styles: { width: '365px' },
				onclick: this._onClickMenuInside
			},
			userDeployInfo
				? this._activeOs === 'linux'
					? this._renderDeployDropdownMenuForLinux(userDeployInfo)
					: this._renderDeployDropdownMenuForWindows(userDeployInfo)
				: this._renderEmptyDeployDropdownMenu()
		);
	}

	private _renderDeployDropdownMenuForLinux(userDeployInfo: DeployInfo) {
		return [
			v('h6', [
				'部署到您的主机',
				v(
					'div',
					{
						classes: [c.btn_group, c.btn_group_toggle, c.btn_group_sm, c.ml_2],
						role: 'group'
					},
					[
						v(
							'button',
							{
								type: 'button',
								classes: [c.btn, c.btn_outline_primary, c.active, css.btnSmall],
								onclick: this._onSelectLinux
							},
							['Linux']
						),
						v(
							'button',
							{
								type: 'button',
								classes: [c.btn, c.btn_outline_primary, css.btnSmall],
								onclick: this._onSelectWindows
							},
							['Windows']
						)
					]
				)
			]),
			v('ol', { classes: [c.pl_3, c.mb_0] }, [
				v('li', [
					'下载并安装 ',
					v('a', { href: `${userDeployInfo.installerLinuxUrl}` }, ['Blocklang-installer'])
				]),
				v('li', [
					'执行',
					v('code', ['./blocklang-installer register']),
					'命令注册主机',
					v('ol', { classes: [c.pl_3] }, [
						v('li', ['指定 URL 为', v('code', [`${userDeployInfo.url}`])]),
						v('li', ['指定注册 Token 为', v('code', [`${userDeployInfo.registrationToken}`])]),
						v('li', ['设置运行端口 <port>'])
					])
				]),
				v('li', ['执行', v('code', ['./blocklang-installer run --port <port>']), '命令启动服务']),
				v('li', ['在浏览器中访问', v('code', ['http://<ip>:<port>'])])
			])
		];
	}

	private _activeOs: string = 'linux'; // linux/windows
	// TODO: 国际化
	private _renderDeployDropdownMenuForWindows(userDeployInfo: DeployInfo) {
		return [
			v('h6', [
				'部署到您的主机',
				v(
					'div',
					{
						classes: [c.btn_group, c.btn_group_toggle, c.btn_group_sm, c.ml_2],
						role: 'group'
					},
					[
						v(
							'button',
							{
								type: 'button',
								classes: [c.btn, c.btn_outline_primary, css.btnSmall],
								onclick: this._onSelectLinux
							},
							['Linux']
						),
						v(
							'button',
							{
								type: 'button',
								classes: [c.btn, c.btn_outline_primary, c.active, css.btnSmall],
								onclick: this._onSelectWindows
							},
							['Windows']
						)
					]
				)
			]),
			v('ol', { classes: [c.pl_3, c.mb_0] }, [
				v('li', [
					'下载并安装 ',
					v('a', { href: `${userDeployInfo.installerWindowsUrl}` }, ['Blocklang-installer'])
				]),
				v('li', [
					'执行',
					v('code', ['blocklang-installer.exe register']),
					'命令注册主机',
					v('ol', { classes: [c.pl_3] }, [
						v('li', ['指定 URL 为', v('code', [`${userDeployInfo.url}`])]),
						v('li', ['指定注册 Token 为', v('code', [`${userDeployInfo.registrationToken}`])]),
						v('li', ['设置运行端口 <port>'])
					])
				]),
				v('li', ['执行', v('code', ['blocklang-installer.exe run --port <port>']), '命令启动服务']),
				v('li', ['在浏览器中访问', v('code', ['http://<ip>:<port>'])])
			])
		];
	}

	private _renderEmptyDeployDropdownMenu() {
		return [w(Spinner, {})];
	}

	private _onSelectLinux() {
		this._activeOs = 'linux';
		this.invalidate();
	}

	private _onSelectWindows() {
		this._activeOs = 'windows';
		this.invalidate();
	}

	// 当点击菜单内部时，不自动关闭此菜单
	private _onClickMenuInside(event: any) {
		event.stopPropagation();
	}

	private _renderTable() {
		return v('div', { classes: [c.card] }, [this._renderLatestCommitInfo(), this._renderResources()]);
	}

	/**
	 *  最近提交信息区
	 */
	private _renderLatestCommitInfo() {
		const { messages } = this._localizedMessages;
		const { latestCommitInfo } = this.properties;
		if (!latestCommitInfo) {
			return;
		}

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

			if (gitStatus === GitFileStatus.Untracked || gitStatus === GitFileStatus.Added) {
				untracked = true;
				statusLetter = '●';
				statusColor = c.text_success;
				statusTooltip = '包含变更的内容';
			} else if (gitStatus === GitFileStatus.Modified || gitStatus === GitFileStatus.Changed) {
				// 如果目录中同时有新增和修改，则显示修改颜色
				// 如果目录中只有新增，则显示新增颜色
				statusLetter = '●';
				statusColor = c.text_warning;
				statusTooltip = '包含变更的内容';
			}
		} else {
			if (gitStatus === GitFileStatus.Untracked || gitStatus === GitFileStatus.Added) {
				untracked = true;
				statusLetter = 'U';
				statusColor = c.text_success;
				statusTooltip = '未跟踪';
			} else if (gitStatus === GitFileStatus.Modified || gitStatus === GitFileStatus.Changed) {
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

	private _renderReadme() {
		const projectFile = { name: 'README.md' };
		const canEditProject = false;

		const { readme = '' } = this.properties;

		return v('div', { classes: [c.card, c.mt_3] }, [
			v('div', { classes: [c.card_header, c.px_2] }, [
				v('div', {}, [w(FontAwesomeIcon, { icon: 'book-open' }), ` ${projectFile.name}`]),
				canEditProject ? w(Link, { to: '' }, [w(FontAwesomeIcon, { icon: 'edit' })]) : null
			]),

			v('div', { classes: [c.card_body, c.markdown_body] }, [w(MarkdownPreview, { value: readme })])
		]);
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
					title: uncommittedFile.iconTitle
				})
			]),
			// 资源名称
			v('td', { classes: [c.px_1] }, [
				v('div', { classes: [css.truncate] }, [
					gitStatus === GitFileStatus.Removed
						? v('del', [`${uncommittedFile.resourceName}`])
						: v('span', [`${uncommittedFile.resourceName}`]),
					v('small', { classes: [c.text_muted, c.ml_1] }, [`${uncommittedFile.parentNamePath}`])
				])
			]),
			v('td', { classes: [css.operator] }, [
				v(
					'span',
					{
						onclick: this._onUnstageChanges
					},
					[
						w(FontAwesomeIcon, {
							icon: 'minus',
							title: '撤销暂存的更改',
							classes: [css.op]
						})
					]
				)
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`])
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
					title: uncommittedFile.iconTitle
				})
			]),
			// 资源名称
			v('td', { classes: [c.px_1] }, [
				v('div', { classes: [css.truncate] }, [
					gitStatus === GitFileStatus.Deleted
						? v('del', [`${uncommittedFile.resourceName}`])
						: v('span', [`${uncommittedFile.resourceName}`]),
					v('small', { classes: [c.text_muted, c.ml_1] }, [`${uncommittedFile.parentNamePath}`])
				])
			]),
			v('td', { classes: [css.operator] }, [
				v(
					'span',
					{
						onclick: this._onStageChanges
					},
					[
						w(FontAwesomeIcon, {
							icon: 'plus',
							title: '暂存更改',
							classes: [css.op]
						})
					]
				)
			]),
			v('td', { classes: [css.status, statusColor], title: `${statusTooltip}` }, [`${statusLetter}`])
		]);
	}

	private _onStageChanges() {
		const { onStageChanges, uncommittedFile } = this.properties;

		onStageChanges(uncommittedFile.fullKeyPath);
	}
}
