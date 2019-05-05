import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import { v, w, dom } from '@dojo/framework/widget-core/d';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import { Project, ProjectResource, CommitInfo, DeployInfo } from '../../interfaces';
import Moment from '../../widgets/moment';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import { IconName, IconPrefix } from '@fortawesome/fontawesome-svg-core';
import MarkdownPreview from '../../widgets/markdown-preview';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';

import * as c from '../../className';
import * as css from './ViewProject.m.css';
import { ProjectPathPayload } from '../../processes/interfaces';

import * as $ from 'jquery';
import Spinner from '../../widgets/spinner';
import ProjectHeader from '../widgets/ProjectHeader';
import { isEmpty } from '../../util';
import Exception from '../error/Exception';
import { ResourceType, GitFileStatus } from '../../constant';
import { Params } from '@dojo/framework/routing/interfaces';

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
	onGetDeployInfo: (opt: ProjectPathPayload) => void;
}

@theme(css)
export default class ViewProject extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		if (this._isNotFound()) {
			return w(Exception, { type: '404' });
		}

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderNavigation(),
			this._renderTable(),
			this._renderReadme()
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
		return v('div', { classes: [c.d_flex, c.justify_content_end, c.mb_2] }, [
			v('div', { classes: [] }, [
				this._renderNewResourceButtonGroup(),
				// 发布按钮，后面显示发布次数
				this._renderReleaseButton(),
				// 部署按钮，显示部署步骤
				this._renderDeployButton()
			])
		]);
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
