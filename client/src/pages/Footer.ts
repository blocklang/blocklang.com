import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as c from '../className';
import * as css from './Footer.m.css';
import FontAwesomeIcon from '../widgets/fontawesome-icon';

export interface FooterProperties {}

@theme(css)
export default class Footer extends ThemedMixin(I18nMixin(WidgetBase))<FooterProperties> {
	protected render() {
		return v(
			'div',
			{
				classes: [
					css.root,
					c.container,
					c.border_top,
					c.my_4,
					c.pt_3,
					c.text_muted,
					c.d_flex,
					c.justify_content_between
				]
			},
			[
				v('div', [`© ${new Date().getFullYear()} BlockLang `]),
				v('div', [
					v('a', { href: 'https://github.com/blocklang', classes: [c.text_muted], title: 'github' }, [
						w(FontAwesomeIcon, { icon: ['fab', 'github'] })
					]),
					v(
						'a',
						{
							href:
								'//shang.qq.com/wpa/qunwpa?idkey=c20cdcd9c2570f0ba969283808b34d983a4d6d7b7bc83f41ead9417e5e4b6c2d',
							classes: [c.ml_2, c.text_muted],
							target: '_blank',
							title: '诚邀志同道合的编程手艺人'
						},
						['QQ群 ', v('strong', ['619312757'])]
					)
				])
			]
		);
	}
}
