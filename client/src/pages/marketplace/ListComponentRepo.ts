import { v, w } from '@dojo/framework/widget-core/d';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import messageBundle from '../../nls/main';
import * as c from '../../className';
import * as css from './ListComponentRepo.m.css';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Link from '@dojo/framework/routing/Link';
import { PagedComponentRepos } from '../../interfaces';
import Spinner from '../../widgets/spinner';
import Exception from '../error/Exception';

export interface ListComponentRepoProperties {
	loggedUsername: string;
	pagedComponentRepos: PagedComponentRepos;
	marketplacePageStatusCode: number;
}

@theme(css)
export default class ListComponentRepo extends ThemedMixin(I18nMixin(WidgetBase))<ListComponentRepoProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { marketplacePageStatusCode } = this.properties;

		if (marketplacePageStatusCode === 404) {
			return w(Exception, { type: '404' });
		}

		const { messages } = this._localizedMessages;
		return v('div', { classes: [css.root, c.container] }, [
			this._renderSearchBox(),

			// 发布按钮
			v('div', { classes: [c.d_flex, c.justify_content_end] }, [
				this._isLogin()
					? w(Link, { to: 'settings-marketplace', classes: [c.btn, c.btn_primary] }, [
							`${messages.publishComponentLabel}`
					  ])
					: undefined
			]),
			// 部件列表
			this._renderCompomentReposBlock(),
			// 分页
			v('div', [])
		]);
	}

	private _renderCompomentReposBlock() {
		const { pagedComponentRepos } = this.properties;

		if (!pagedComponentRepos) {
			return w(Spinner, {});
		}

		if (pagedComponentRepos.content.length === 0) {
			return this._renderEmptyComponentRepo();
		}

		return this._renderComponentRepos();
	}

	private _renderSearchBox() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.mt_5, c.mb_3] }, [
			// 标题
			v('h1', { classes: [c.text_center, c.font_weight_light] }, [`${messages.marketplaceTitle}`]),
			v('div', { classes: [c.row, c.mt_3] }, [
				v('div', { classes: [c.mx_auto, c.col_sm_12, c.col_lg_6] }, [
					v('input', {
						type: 'text',
						classes: [c.form_control],
						'aria-describedby': 'btnSearch',
						placeholder: `${messages.componentSearchPlaceholder}`
					}),
					v('small', { classes: [c.form_text, c.text_muted] }, [
						w(FontAwesomeIcon, { icon: 'lightbulb' }),
						` ${messages.componentSearchHelp}`
					])
				])
			])
		]);
	}

	private _renderEmptyComponentRepo() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.jumbotron, c.mx_auto, c.text_center, c.mt_3], styles: { maxWidth: '544px' } }, [
			w(FontAwesomeIcon, { icon: 'puzzle-piece', size: '2x', classes: [c.text_muted] }),
			v('h3', { classes: [c.mt_3] }, [`${messages.noComponentTitle}`]),
			v('p', [
				v('ol', { classes: [c.text_left] }, [
					v('li', [`${messages.noComponentTipLine1}`]),
					v('li', [`${messages.noComponentTipLine2}`]),
					v('li', [`${messages.noComponentTipLine3}`])
				])
			]),
			this._isLogin()
				? w(
						Link,
						{
							classes: [c.btn, c.btn_secondary, c.btn_sm],
							to: 'settings-marketplace'
						},
						[`${messages.publishComponentLabel}`]
				  )
				: undefined
		]);
	}

	private _isLogin() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderComponentRepos() {
		return v('div', { classes: [c.mt_3] }, []);
	}
}
