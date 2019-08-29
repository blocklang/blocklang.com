// 以下代码，参考 https://github.com/ant-design/ant-design-pro/blob/master/src/components/Exception/index.js

import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import Link from '@dojo/framework/routing/Link';
import config from './config';

import * as css from './Exception.m.css';
import * as c from '../../className';

export interface ExceptionProperties {
	type: PageType;
}

export type PageType = '403' | '404' | '500';

@theme(css)
export default class Exception extends ThemedMixin(I18nMixin(WidgetBase))<ExceptionProperties> {
	protected render() {
		const { type: pageType } = this.properties;

		return v('div', { classes: [css.root] }, [
			v('div', { classes: [css.imgBlock] }, [
				v('div', {
					classes: [css.imgEle],
					styles: { backgroundImage: `url(${(config as any)[pageType].img})` }
				})
			]),
			v('div', { classes: [css.content] }, [
				v('h1', [`${config[pageType].title}`]),
				v('div', { classes: [css.desc] }, [`${config[pageType].desc}`]),
				v('div', { classes: [css.action] }, [
					w(Link, { to: 'home', classes: [c.btn, c.btn_primary, c.btn_sm] }, [`返回首页`])
				])
			])
		]);
	}
}
