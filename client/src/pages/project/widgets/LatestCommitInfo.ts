import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { CommitInfo } from '../../../interfaces';
import * as css from './LatestCommitInfo.m.css';
import { v, w } from '@dojo/framework/core/vdom';
import Link from '@dojo/framework/routing/Link';
import Moment from '../../../widgets/moment';
import * as c from '../../../className';
import messageBundle from '../../../nls/main';

export interface LatestCommitInfoProperties {
	latestCommitInfo: CommitInfo;
	showBottomBorder?: boolean;
}

@theme(css)
export default class LatestCommitInfo extends ThemedMixin(I18nMixin(WidgetBase))<LatestCommitInfoProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { latestCommitInfo, showBottomBorder = false } = this.properties;
		if (!latestCommitInfo) {
			return;
		}
		const { messages } = this._localizedMessages;
		return v(
			'div',
			{
				classes: [
					c.card_header,
					c.text_muted,
					c.px_2,
					showBottomBorder ? undefined : c.border_bottom_0,
					css.recentCommit
				]
			},
			[
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
			]
		);
	}
}
