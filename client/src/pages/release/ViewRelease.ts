import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import watch from '@dojo/framework/widget-core/decorators/watch';

//import messageBundle from '../../nls/main';

import * as SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { v } from '@dojo/framework/widget-core/d';

import * as c from '../../className';
import * as css from './ViewRelease.m.css';
import { ProjectRelease, Project } from '../../interfaces';
import { baseUrl } from '../../config';
import { getHeaders } from '../../processes/utils';

type WsEvent = 'console' | 'finish';

interface WsMessageHeader {
	lineNum: number;
	event: WsEvent;
}

interface WsMessage {
	payload: string;
	headers: WsMessageHeader;
}

export interface ViewReleaseProperties {
	project: Project;
	projectRelease: ProjectRelease;
}

const client = new Client({
	debug: function(str) {
		console.log(str);
	}
});

client.webSocketFactory = function() {
	return new SockJS('/release-console');
};

client.onStompError = function(frame) {
	console.log('Broker reported error: ' + frame.headers['message']);
	console.log('Additional details: ' + frame.body);
};

@theme(css)
export default class ViewRelease extends ThemedMixin(I18nMixin(WidgetBase))<ViewReleaseProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	private _logLoaded: boolean = false;
	private _watingConsole: WsMessage[] = [];

	// 注意，watch 只能监听重新设置值，不能监听数组大小的变化
	// 而 console 中只增加元素，所以加 @watch 不会起作用
	@watch()
	private _logs: string[] = []; // 存历史日志

	private _console: string[] = []; // 存实时刷新的日志

	protected render() {
		const { projectRelease } = this.properties;

		if (projectRelease) {
			if (projectRelease.releaseResult === '02' /* started */) {
				// 正在发布中，实时显示控制台信息
				if (!client.active && projectRelease) {
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
					client.onConnect = (frame) => {
						client.subscribe('/topic/releases/' + taskId, (message) => {
							const body = JSON.parse(message.body);
							const {
								payload,
								headers: { event }
							} = body as WsMessage;
							if (event === 'finish') {
								// 如果已读完，则关闭
								client.deactivate();
							} else if (event === 'console') {
								// 如果历史日志已加载完成，则开始渲染实时日志
								// 如果历史日志没有加载完成，但已经收到了实时日志，则等待历史日志加载完成
								// 日志日志加载完成后，要判断有没有行丢失，有没有行重复，或者正好可以接上
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
					client.activate();
				}
			} else {
				// 已发布完成，只加载历史记录
				if (this._logs.length === 0) {
					this._fetchLog();
				}
			}
		}

		return v('div', { classes: [c.container] }, [
			v('div', { classes: [css.logBody] }, [
				v('pre', {}, [
					...this._logs.map((lineContent) => this._renderLine(lineContent)),
					...this._console.map((lineContent) => this._renderLine(lineContent))
				])
			])
		]);
	}

	private _renderLine(lineContent: string) {
		return v('div', { key: '', classes: [css.logLine] }, [v('a'), v('span', [lineContent])]);
	}

	private async _fetchLog(endLine?: number) {
		const {
			project: { createUserName: owner, name: project },
			projectRelease: { version }
		} = this.properties;
		let url = `${baseUrl}/projects/${owner}/${project}/releases/${version}/log`;
		if (endLine) {
			url += `?end_line=${endLine}`;
		}
		const response = await fetch(url, {
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
}
