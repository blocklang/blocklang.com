import { v, w } from '@dojo/framework/core/vdom';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/core/mixins/Themed';
import messageBundle from '../../nls/main';
import RepositoryHeader from '../widgets/RepositoryHeader';
import { Repository, ProjectRelease, WsMessage } from '../../interfaces';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import Link from '@dojo/framework/routing/Link';
import Spinner from '../../widgets/spinner';
import { IconName } from '@fortawesome/fontawesome-svg-core';
import MarkdownPreview from '../../widgets/markdown-preview';
import Moment from '../../widgets/moment';
import { find } from '@dojo/framework/shim/array';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';
import * as c from '@blocklang/bootstrap-classes';
import * as css from './ListRelease.m.css';
import SockJS = require('sockjs-client');
import { Client, IFrame } from '@stomp/stompjs';
import { ReleaseResult } from '../../constant';
import { canRelease } from '../../permission';

export interface ListReleaseProperties {
	loggedUsername: string;
	repository: Repository;
	releases: ProjectRelease[];
}

@theme(css)
export default class ListRelease extends ThemedMixin(I18nMixin(WidgetBase))<ListReleaseProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	private _wsClient: Client;
	private _runningReleases: any[] = [];

	constructor() {
		super();
		this._wsClient = new Client({});

		this._wsClient.webSocketFactory = function () {
			return new SockJS('/release-console');
		};

		this._wsClient.onStompError = function (frame: IFrame) {
			console.log('Broker reported error: ' + frame.headers['message']);
			console.log('Additional details: ' + frame.body);
		};
	}

	protected onDetach() {
		if (this._wsClient.active) {
			this._wsClient.deactivate();
		}
	}

	protected render() {
		return v('div', { classes: [css.root, c.container] }, [this._renderHeader(), this._renderReleasesPart()]);
	}

	private _isAuthenticated() {
		const { repository, loggedUsername } = this.properties;
		const isLogin = !!loggedUsername;
		if (!isLogin) {
			return false;
		}
		return canRelease(repository.accessLevel);
	}

	private _renderReleasesPart() {
		const { releases } = this.properties;
		if (releases === undefined) {
			return w(Spinner, {});
		}
		if (releases.length === 0) {
			return this._renderEmptyReleases();
		}
		return this._renderReleases(releases);
	}

	private _renderHeader() {
		const {
			messages: { privateRepositoryTitle },
		} = this._localizedMessages;
		const { repository } = this.properties;

		return w(RepositoryHeader, { repository, privateRepositoryTitle });
	}

	private _renderEmptyReleases() {
		const {
			messages: { releaseText, noReleaseTitle, newReleaseText, noReleaseTip },
		} = this._localizedMessages;
		const {
			repository: { createUserName, name },
		} = this.properties;

		return v('div', { key: 'empty', classes: [c.container], styles: { maxWidth: '700px' } }, [
			v('div', { classes: [c.pb_4, c.mb_4, c.border_bottom] }, [
				w(
					Link,
					{
						classes: [c.btn, c.btn_info],
						to: 'list-release',
						params: { owner: createUserName, project: name },
					},
					[`${releaseText}`]
				),
			]),
			v('div', { classes: [c.jumbotron, c.mx_auto, c.text_center], styles: { maxWidth: '544px' } }, [
				w(FontAwesomeIcon, { icon: 'tag', size: '2x', classes: [c.text_muted] }),
				v('h3', { classes: [c.mt_3] }, [`${noReleaseTitle}`]),
				v('p', {}, [`${noReleaseTip}`]),
				this._isAuthenticated()
					? w(
							Link,
							{
								classes: [c.btn, c.btn_secondary, c.btn_sm],
								to: 'new-release',
								params: { owner: createUserName, project: name },
							},
							[`${newReleaseText}`]
					  )
					: undefined,
			]),
		]);
	}

	// TODO: 需增加：如果在另一个浏览器有新增发布，则在当前浏览器上应显示出新增任务
	private _renderReleases(releases: ProjectRelease[]) {
		// 监听发布状态
		if (this._runningReleases.length === 0) {
			this._runningReleases = releases.filter((item) => item.releaseResult === ReleaseResult.Started);
			if (this._runningReleases.length > 0) {
				if (!this._wsClient.active) {
					this._wsClient.onConnect = (frame: IFrame) => {
						this._runningReleases.forEach((item) => {
							this._wsClient.subscribe('/topic/releases/' + item.id, (message) => {
								const body = JSON.parse(message.body);
								const {
									headers: { event, releaseResult },
								} = body as WsMessage;
								if (event === 'finish') {
									item.releaseResult = releaseResult;
									this.invalidate();
								}
							});
						});
					};
					this._wsClient.activate();
				}
			}
		}

		return v('div', { key: 'releases' }, [
			this._renderReleaseHeader(),
			v(
				'div',
				{ classes: [c.border_top] },
				releases.map((release) => this._renderItem(release))
			),
		]);
	}

	private _renderReleaseHeader() {
		const {
			messages: { newReleaseText },
		} = this._localizedMessages;
		const {
			repository: { createUserName, name },
		} = this.properties;

		return v('div', { classes: [c.pb_4, c.d_flex, c.justify_content_end] }, [
			v('div', { classes: [] }, [
				this._isAuthenticated()
					? w(
							Link,
							{
								classes: [c.btn, c.btn_outline_secondary],
								to: 'new-release',
								params: { owner: createUserName, project: name },
							},
							[`${newReleaseText}`]
					  )
					: v(
							'a',
							{
								classes: [c.btn, c.btn_outline_secondary, c.disabled],
								tabIndex: -1,
								'aria-disabled': 'true',
							},
							[`${newReleaseText}`]
					  ),
			]),
		]);
	}

	private _renderItem(release: ProjectRelease) {
		const { repository } = this.properties;
		let releaseResult = release.releaseResult;
		// 如果初始化时是处于运行状态，则监听状态是否有发生变化
		if (releaseResult === '02') {
			const matched = find(this._runningReleases, (item) => item.id === release.id);
			if (matched) {
				releaseResult = matched.releaseResult;
			}
		}

		let resultClasses = '';
		let spin = false;
		let resultText = '';
		let icon: IconName = 'clock';
		if (releaseResult === ReleaseResult.Inited) {
			resultClasses = c.text_muted;
			resultText = '准备';
			icon = 'clock';
		} else if (releaseResult === ReleaseResult.Started) {
			spin = true;
			resultClasses = c.text_warning;
			resultText = '发布中';
			icon = 'spinner';
		} else if (releaseResult === ReleaseResult.Failed) {
			resultClasses = c.text_danger;
			resultText = '失败';
			icon = 'times';
		} else if (releaseResult === ReleaseResult.Passed) {
			resultClasses = c.text_success;
			resultText = '成功';
			icon = 'check';
		} else if (releaseResult === ReleaseResult.Canceled) {
			resultClasses = c.text_muted;
			resultText = '取消';
			icon = 'ban';
		}

		return v('div', { classes: [c.row] }, [
			v('div', { classes: [c.d_none, c.d_md_block, c.col_12, c.col_md_3, c.text_right, c.py_4] }, [
				v('ul', { classes: [c.list_unstyled, c.mt_2] }, [
					v('li', { classes: [c.mb_1] }, [
						w(FontAwesomeIcon, { icon: 'tag', classes: [c.text_muted] }),
						` ${release.version}`,
					]),
					v('li', { classes: [c.mb_1, resultClasses] }, [
						w(FontAwesomeIcon, { icon, spin }),
						` ${resultText}`,
					]),
				]),
			]),
			v('div', { classes: [c.col_12, c.col_md_9, css.releaseMainSection, c.py_4] }, [
				// header
				v('h2', [
					w(
						Link,
						{
							to: 'view-release',
							params: {
								owner: repository.createUserName,
								project: repository.name,
								version: release.version,
							},
							classes: [resultClasses],
						},
						[`${release.title}`]
					),
				]),
				v('ul', { classes: [c.d_md_none, c.list_inline, c.mb_1] }, [
					v('li', { classes: [c.list_inline_item, c.mb_1] }, [
						w(FontAwesomeIcon, { icon: 'tag', classes: [c.text_muted] }),
						` ${release.version}`,
					]),
					v('li', { classes: [c.list_inline_item, c.mb_1, resultClasses] }, [
						w(FontAwesomeIcon, { icon, spin }),
						` ${resultText}`,
					]),
				]),
				v('div', { classes: [c.mb_4] }, [
					v('small', { classes: [c.text_muted] }, [
						w(Link, { to: `${release.createUserName}` }, [
							v(
								'img',
								{ classes: [c.avatar], src: `${release.createUserAvatarUrl}`, width: 20, height: 20 },
								[]
							),
							' ',
							v('strong', [`${release.createUserName}`]),
						]),
						' 发布于 ',
						w(Moment, { datetime: release.createTime }),
					]),
				]),
				// 介绍
				release.description
					? v('div', { classes: [c.markdown_body] }, [w(MarkdownPreview, { value: release.description })])
					: null,
				// jdk
				v('hr'),
				v('div', { classes: [c.text_muted, c.my_4] }, [
					w(FontAwesomeIcon, { icon: ['fab', 'java'], size: '2x' }),
					` ${release.jdkName}_${release.jdkVersion} `,
				]),
			]),
		]);
	}
}
