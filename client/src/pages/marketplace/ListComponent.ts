import { v, w } from '@dojo/framework/widget-core/d';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import messageBundle from '../../nls/main';
import * as c from '../../className';
import * as css from './ListMarketplace.m.css';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Link from '@dojo/framework/routing/Link';
import { Component } from '../../interfaces';
import Spinner from '../../widgets/spinner';

export interface ListComponentProperties {
	loggedUsername: string;
	components: Component[];
}

@theme(css)
export default class ListComponent extends ThemedMixin(I18nMixin(WidgetBase))<ListComponentProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { messages } = this._localizedMessages;
		return v('div', { classes: [css.root, c.container] }, [
			this._renderSearchBox(),

			// 发布按钮
			v('div', { classes: [c.d_flex, c.justify_content_end] }, [
				this._isLogin()
					? w(Link, { to: '#', classes: [c.btn, c.btn_primary] }, [`${messages.publishComponentLabel}`])
					: undefined
			]),
			// 部件列表
			this._renderCompomentsBlock(),
			// 分页
			v('div', [])
		]);
	}

	private _renderCompomentsBlock() {
		const { components } = this.properties;

		if (!components) {
			return w(Spinner, {});
		}

		if (components.length === 0) {
			return this._renderEmptyComponent();
		}

		return this._renderComponents();
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

	private _renderEmptyComponent() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.jumbotron, c.mx_auto, c.text_center], styles: { width: '544px' } }, [
			w(FontAwesomeIcon, { icon: 'puzzle-piece', size: '2x', classes: [c.text_muted] }),
			v('h3', { classes: [c.mt_3] }, [`${messages.noComponentTitle}`]),
			v('p', [`${messages.noComponentTip}`]),
			this._isLogin()
				? w(
						Link,
						{
							classes: [c.btn, c.btn_secondary, c.btn_sm],
							to: 'new-component'
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

	private _renderComponents() {
		return v('div', { classes: [c.mt_3] }, []);
	}
}
