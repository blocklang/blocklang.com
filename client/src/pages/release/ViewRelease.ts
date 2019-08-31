import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import watch from '@dojo/framework/core/decorators/watch';

import messageBundle from '../../nls/main';

import * as SockJS from 'sockjs-client';
import { Client, IFrame } from '@stomp/stompjs';
import { v, w } from '@dojo/framework/core/vdom';

import * as c from '../../className';
import * as css from './ViewRelease.m.css';
import { ProjectRelease, Project, WsMessage } from '../../interfaces';
import { baseUrl } from '../../config';
import { getHeaders } from '../../processes/utils';
import { IconName } from '@fortawesome/fontawesome-svg-core';
import FontAwesomeIcon from 'dojo-fontawesome/FontAwesomeIcon';
import Link from '@dojo/framework/routing/Link';
import Moment from '../../widgets/moment';
import MarkdownPreview from '../../widgets/markdown-preview';
import ProjectHeader from '../widgets/ProjectHeader';
import { ReleaseResult } from '../../constant';

export interface ViewReleaseProperties {
	project: Project;
	projectRelease: ProjectRelease;
}

@theme(css)
export default class ViewRelease extends ThemedMixin(I18nMixin(WidgetBase))<ViewReleaseProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	private _logLoaded: boolean = false;
	private _watingConsole: WsMessage[] = [];

	// 注意，watch 只能监听重新设置值，不能监听数组大小的变化
	// 而 console 中只增加元素，所以加 @watch 不会起作用
	@watch()
	private _logs: string[] = []; // 存历史日志

	private _console: string[] = []; // 存实时刷新的日志

	private _releaseResult: string = '';

	private _wsClient: Client;

	constructor() {
		super();
		this._wsClient = new Client({});

		this._wsClient.webSocketFactory = function() {
			return new SockJS('/release-console');
		};

		this._wsClient.onStompError = function(frame: IFrame) {
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
		const { projectRelease } = this.properties;

		if (!projectRelease) {
			return;
		}

		if (this._releaseResult === '') {
			// 使用从部件外传来的值初始化
			this._releaseResult = projectRelease.releaseResult;
		}
		if (this._releaseResult === ReleaseResult.Started /* started */) {
			// 正在发布中，实时显示控制台信息
			if (!this._wsClient.active && projectRelease) {
				// 这里要解决好两方面问题：
				// 1. 日志的内容要完整，不要出现缺失或重复
				// 2. 选择好获取历史日志的时机，以避免出现第一种情况。
				//
				// 关于获取历史日志的时机
				// 1. 之前考虑当收到第一个，当收到第一条返回的消息，然后根据消息返回的记录行来精确获取历史日志
				//    但是发现这有一定的不确定性，因为构建的不同阶段，有时会很快返回消息，有时要等很久，
				//    此时页面的控制台中长时间没有内容显示。
				// 2. 因此将时机调整为 subscribe 成功之后，但此种情况可能会出现日志重复的情况，因此需要减去重复的行
				//    这种情况就不能精确传入行了

				const taskId = projectRelease.id || 0;
				this._wsClient.onConnect = (frame: IFrame) => {
					this._wsClient.subscribe('/topic/releases/' + taskId, (message) => {
						const body = JSON.parse(message.body);
						const {
							payload,
							headers: { event, releaseResult }
						} = body as WsMessage;
						if (event === 'finish') {
							// 如果已读完，则关闭
							this._wsClient.deactivate();
							this._releaseResult = releaseResult!;
							this.invalidate();
						} else if (event === 'console') {
							// 如果历史日志已加载完成，则开始渲染实时日志
							// 如果历史日志没有加载完成，但已经收到了实时日志，则等待历史日志加载完成
							// 历史日志加载完成后，要判断有没有行丢失，有没有行重复，或者正好可以接上
							if (!this._logLoaded) {
								this._watingConsole.push(body);
							} else {
								const logLineCount = this._logs.length; // 行号是从 0 开始计算的
								if (this._watingConsole.length > 0) {
									this._watingConsole
										.filter((item) => {
											const lineNum = item.headers.lineNum;
											if (lineNum === logLineCount) {
												console.log(
													`历史日志的结束行是 ${logLineCount -
														1}，实时日志的开始行是 ${lineNum},正好接上。`
												);
												return true;
											} else if (lineNum > logLineCount) {
												console.log(
													`历史日志的结束行是 ${logLineCount -
														1}，实时日志的开始行是 ${lineNum},中间出现缺失。`
												);
												return true;
											} else {
												console.log(
													`历史日志的结束行是 ${logLineCount -
														1}，实时日志的开始行是 ${lineNum},中间出现重复。`
												);
												// 去重
												return false;
											}
										})
										.forEach((item) => {
											this._appendToConsole(item.payload);
										});
									this._watingConsole = [];
									this.invalidate();
								} else {
									this._appendToConsole(payload);
									this.invalidate();
								}
							}
						}
					});

					this._fetchLog();
				};
				this._wsClient.activate();
			}
		} else if (
			this._releaseResult === ReleaseResult.Failed ||
			this._releaseResult === ReleaseResult.Passed ||
			this._releaseResult === ReleaseResult.Canceled
		) {
			// 已发布完成，只加载历史记录
			if (!this._logLoaded) {
				this._fetchLog();
			}
		}

		return v('div', { classes: [c.container] }, [this._renderHeader(), this._renderReleasePart()]);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderReleasePart() {
		return v('div', [
			this._renderReleaseHeader(),
			v('div', [this._renderReleaseInfo(), this._renderReleaseLog(), this._renderScrollToEndLineAnchor()])
		]);
	}

	private _renderReleaseHeader() {
		const {
			messages: { releaseText, newReleaseText }
		} = this._localizedMessages;
		const {
			project: { createUserName, name }
		} = this.properties;

		return v('div', { classes: [c.pb_4, c.d_flex, c.justify_content_between] }, [
			v('div', {}, [
				w(
					Link,
					{
						classes: [c.btn, c.btn_info],
						to: 'list-release',
						params: { owner: createUserName, project: name }
					},
					[`${releaseText}`]
				)
			]),
			v('div', { classes: [] }, [
				w(
					Link,
					{
						classes: [c.btn, c.btn_outline_secondary],
						to: 'new-release',
						params: { owner: createUserName, project: name }
					},
					[`${newReleaseText}`]
				)
			])
		]);
	}

	private _renderReleaseLog() {
		return v('div', { classes: [css.logBody] }, [
			v('pre', {}, [
				...this._logs.map((lineContent) => this._renderLine(lineContent)),
				...this._console.map((lineContent) => this._renderLine(lineContent))
			])
		]);
	}

	private _renderScrollToEndLineAnchor() {
		return v('div', {
			scrollIntoView: () => {
				return this._releaseResult === ReleaseResult.Started;
			}
		});
	}

	private _renderLine(lineContent: string) {
		return v('div', { key: '', classes: [css.logLine] }, [v('a'), v('span', [lineContent])]);
	}

	private async _fetchLog() {
		const {
			project: { createUserName: owner, name: project },
			projectRelease: { version }
		} = this.properties;

		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/releases/${version}/log`, {
			headers: getHeaders()
		});
		const json = await response.json();
		if (response.ok) {
			this._logs = json;
		} else {
			this._logs = [];
		}
		this._logLoaded = true;
	}

	private _appendToConsole(lineContent: string) {
		this._console.push(lineContent);
	}

	private _renderReleaseInfo() {
		const { projectRelease: release } = this.properties;

		const releaseResult = this._releaseResult;

		let borderColorClass = '';
		let resultClasses = '';
		let spin = false;
		let resultText = '';
		let icon: IconName = 'clock';
		if (releaseResult === ReleaseResult.Inited) {
			borderColorClass = '';
			resultClasses = c.text_muted;
			resultText = '准备';
			icon = 'clock';
		} else if (releaseResult === ReleaseResult.Started) {
			spin = true;
			borderColorClass = c.border_warning;
			resultClasses = c.text_warning;
			resultText = '发布中';
			icon = 'spinner';
		} else if (releaseResult === ReleaseResult.Failed) {
			borderColorClass = c.border_danger;
			resultClasses = c.text_danger;
			resultText = '失败';
			icon = 'times';
		} else if (releaseResult === ReleaseResult.Passed) {
			borderColorClass = c.border_success;
			resultClasses = c.text_success;
			resultText = '成功';
			icon = 'check';
		} else if (releaseResult === ReleaseResult.Canceled) {
			borderColorClass = '';
			resultClasses = c.text_muted;
			resultText = '取消';
			icon = 'ban';
		}

		return v('section', { classes: [c.border, borderColorClass, c.mb_4, css.borderLeft] }, [
			v('div', { classes: [c.row] }, [
				v('div', { classes: [c.col_3, c.text_right, c.py_4] }, [
					v('ul', { classes: [c.list_unstyled, c.mt_2] }, [
						v('li', { classes: [c.mb_1] }, [
							w(FontAwesomeIcon, { icon: 'tag', classes: [c.text_muted] }),
							` ${release.version}`
						]),
						v('li', { classes: [c.mb_1, resultClasses] }, [
							w(FontAwesomeIcon, { icon, spin }),
							` ${resultText}`
						])
					])
				]),
				v('div', { classes: [c.col_9, css.releaseMainSection, c.py_4] }, [
					// header
					v('h2', { classes: [resultClasses] }, [`${release.title}`]),
					v('div', { classes: [c.mb_4] }, [
						v('small', { classes: [c.text_muted] }, [
							w(Link, { to: `${release.createUserName}` }, [
								v(
									'img',
									{
										classes: [c.avatar],
										src: `${release.createUserAvatarUrl}`,
										width: 20,
										height: 20
									},
									[]
								),
								' ',
								v('strong', [`${release.createUserName}`])
							]),
							' 发布于 ',
							w(Moment, { datetime: release.createTime })
						])
					]),
					// 介绍
					release.description
						? v('div', { classes: [c.markdown_body] }, [w(MarkdownPreview, { value: release.description })])
						: null,
					// jdk
					v('hr'),
					v('div', { classes: [c.text_muted, c.my_4] }, [
						w(FontAwesomeIcon, { icon: ['fab', 'java'], size: '2x' }),
						` ${release.jdkName}_${release.jdkVersion} `
					])
				])
			])
		]);
	}
}
