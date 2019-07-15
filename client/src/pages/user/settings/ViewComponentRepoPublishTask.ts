import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import messageBundle from '../../../nls/main';
import * as c from '../../../className';
import * as css from './ViewComponentRepoPublishTask.m.css';
import { v, w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';
import { ComponentRepoPublishTask } from '../../../interfaces';
import Exception from '../../error/Exception';
import FontAwesomeIcon from '../../../widgets/fontawesome-icon';
import Moment from '../../../widgets/moment';
import { PublishType, ReleaseResult } from '../../../constant';
import { IconName } from '@fortawesome/fontawesome-svg-core';
import { baseUrl } from '../../../config';
import { getHeaders } from '../../../processes/utils';
import watch from '@dojo/framework/widget-core/decorators/watch';

export interface ViewComponentRepoPublishTaskProperties {
	loggedUsername: string;
	publishTask: ComponentRepoPublishTask;
}

@theme(css)
export default class ViewComponentRepoPublishTask extends ThemedMixin(I18nMixin(WidgetBase))<
	ViewComponentRepoPublishTaskProperties
> {
	private _localizedMessages = this.localizeBundle(messageBundle);
	private _logLoaded: boolean = false;
	@watch()
	private _logs: string[] = []; // 存历史日志

	protected render() {
		if (!this._isAuthenticated()) {
			return w(Exception, { type: '403' });
		}

		if (!this._loginUserIsCreator()) {
			return w(Exception, { type: '403' });
		}

		const { publishTask } = this.properties;
		if (!publishTask) {
			return;
		}

		const publishResult = publishTask.publishResult;

		if (
			publishResult === ReleaseResult.Failed ||
			publishResult === ReleaseResult.Passed ||
			publishResult === ReleaseResult.Canceled
		) {
			if (!this._logLoaded) {
				this._fetchLog();
			}
		}

		return v('div', { classes: [css.root, c.container, c.mt_5] }, [
			v('div', { classes: [c.row] }, [
				v('div', { classes: [c.col_3] }, [this._renderMenu()]),
				v('div', { classes: [c.col_9] }, [
					this._renderHeader(),
					this._renderTaskInfo(),
					this._renderPublishLog()
				])
			])
		]);
	}

	private async _fetchLog() {
		const { publishTask } = this.properties;

		const response = await fetch(`${baseUrl}/marketplace/publish/${publishTask.id}/log`, {
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

	private _isAuthenticated() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _loginUserIsCreator() {
		const { loggedUsername, publishTask } = this.properties;
		return publishTask && loggedUsername === publishTask.createUserName;
	}

	private _renderMenu() {
		const { messages } = this._localizedMessages;

		return v('ul', { classes: [c.list_group] }, [
			v('li', { classes: [c.list_group_item] }, [
				w(Link, { to: 'settings-profile' }, [`${messages.userSettingMenuProfile}`])
			]),
			v('li', { classes: [c.list_group_item, css.active] }, [`${messages.userSettingMenuMarketplace}`])
		]);
	}

	private _renderHeader() {
		const { publishTask } = this.properties;
		const { messages } = this._localizedMessages;
		return v('div', [
			v('h4', [
				v('nav', { 'aria-label': 'breadcrumb' }, [
					v('ol', { classes: [c.breadcrumb, css.navOl] }, [
						v('li', { classes: [c.breadcrumb_item] }, [
							w(Link, { to: 'settings-marketplace' }, [`${messages.userSettingMenuMarketplace}`])
						]),
						v('li', { classes: [c.breadcrumb_item] }, [`${messages.componentRepoPublishMenuItem}`]),
						v('li', { classes: [c.breadcrumb_item] }, [`${publishTask.website}`]),
						v('li', { classes: [c.breadcrumb_item] }, [`${publishTask.owner}`]),
						v('li', { classes: [c.breadcrumb_item] }, [`${publishTask.repoName}`])
					])
				])
			]),
			v('hr')
		]);
	}

	private _renderTaskInfo() {
		const { publishTask } = this.properties;

		const { publishResult, publishType, fromVersion, toVersion } = publishTask;

		let borderColorClass = '';
		let resultClasses = '';
		let spin = false;
		let resultText = '';
		let icon: IconName = 'clock';
		if (publishResult === ReleaseResult.Inited) {
			borderColorClass = '';
			resultClasses = c.text_muted;
			resultText = '准备';
			icon = 'clock';
		} else if (publishResult === ReleaseResult.Started) {
			spin = true;
			borderColorClass = c.border_warning;
			resultClasses = c.text_warning;
			if (publishType === PublishType.New) {
				resultText = '首次发布';
			} else if (publishType === PublishType.Upgrade) {
				resultText = `升级 ${fromVersion} -> ${toVersion}`;
			}
			icon = 'spinner';
		} else if (publishResult === ReleaseResult.Failed) {
			borderColorClass = c.border_danger;
			resultClasses = c.text_danger;
			resultText = '失败';
			icon = 'times';
		} else if (publishResult === ReleaseResult.Passed) {
			borderColorClass = c.border_success;
			resultClasses = c.text_success;
			resultText = '成功';
			icon = 'check';
		} else if (publishResult === ReleaseResult.Canceled) {
			borderColorClass = '';
			resultClasses = c.text_muted;
			resultText = '取消';
			icon = 'ban';
		}

		return v('div', { classes: [c.d_flex, c.border, borderColorClass, c.rounded, c.p_2, c.mb_4] }, [
			v('div', { classes: [c.mr_2, resultClasses] }, [w(FontAwesomeIcon, { icon, spin })]),

			v('div', { classes: [c.flex_grow_1] }, [
				v('div', {}, [
					v('a', { href: `${publishTask.gitUrl}`, classes: [c.font_weight_bold, c.mr_3], target: '_blank' }, [
						`${publishTask.gitUrl}`
					]),
					v('span', { classes: [resultClasses] }, [`${resultText}`])
				]),
				v('div', { classes: [c.text_muted, c.mt_2] }, [
					v('span', { classes: [c.mr_2], title: '任务编号' }, [`#${publishTask.seq}`]),
					v('span', {}, [
						w(FontAwesomeIcon, { icon: 'clock', classes: [c.mr_1] }),
						w(Moment, { datetime: publishTask.startTime })
					])
				])
			])
		]);
	}

	private _renderPublishLog() {
		return v('div', { classes: [css.logBody] }, [
			v('pre', {}, [...this._logs.map((lineContent) => this._renderLine(lineContent))])
		]);
	}

	private _renderLine(lineContent: string) {
		return v('div', { key: '', classes: [css.logLine] }, [v('a'), v('span', [lineContent])]);
	}
}
