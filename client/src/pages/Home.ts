import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/core/mixins/Themed';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './Home.m.css';

import PrivateHome from './user/Home';
import messageBundle from '../nls/main';
import { Repository } from '../interfaces';
import { baseUrl } from '../config';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';

export interface HomeProperties {
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
	canAccessRepos: Repository[];
	loginFailure: boolean;
	loginFailureMessage: string;
}

/**
 * 首页
 *
 * 根据用户是否登录，首页分为公共首页和个人首页。
 * 当用户未登录时，显示公共首页；当用户已登录时，在个人首页显示登录用户相关信息。
 */
@theme(css)
export default class Home extends ThemedMixin(I18nMixin(WidgetBase))<HomeProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { isAuthenticated } = this.properties;
		return isAuthenticated ? this._renderPrivateHome() : this._renderPublicHome();
	}

	private _renderPublicHome() {
		const { loginFailure, loginFailureMessage } = this.properties;
		const { messages } = this._localizedMessages;

		return [
			loginFailure
				? v('div', { classes: [c.alert, c.alert_danger, c.alert_dismissible], role: 'alert' }, [
						v('h4', { classes: [c.alert_heading] }, ['登录失败！']),
						v('p', {}, [`${loginFailureMessage}`]),
						v(
							'button',
							{ type: 'button', classes: [c.close], 'data-dismiss': 'alert', 'aria-label': 'Close' },
							[v('span', { 'aria-hidden': 'true', innerHTML: '&times;' }, [])]
						),
				  ])
				: undefined,
			v('div', { classes: [css.jumbotron, c.jumbotron, c.mt_5] }, [
				v('div', { classes: [c.container, css.jumbotronContent] }, [
					v('h1', { classes: [css.header] }, [messages.blockLangIntro]),
					v('p', { classes: [c.font_weight_normal, css.lead], innerHTML: messages.blockLangDescription }, []),
					v('p', { classes: [c.mt_5, c.text_muted, css.free] }, [messages.free]),
					v('p', {}, [
						v(
							'a',
							{
								classes: [c.btn, c.btn_lg, c.btn_outline_primary, css.loginButton, c.mb_2],
								href: `${baseUrl}/oauth2/authorization/github`,
							},
							[w(FontAwesomeIcon, { icon: ['fab', 'github'], size: 'lg' }), ` ${messages.loginGithub}`]
						),
						' ', // 加一个空格，两个按钮水平摆放时，会有完美间隔。
						v(
							'a',
							{
								classes: [c.btn, c.btn_lg, c.btn_outline_primary, css.loginButton, c.mb_2],
								href: `${baseUrl}/oauth2/authorization/qq`,
							},
							[w(FontAwesomeIcon, { icon: ['fab', 'qq'], size: 'lg' }), ` ${messages.loginQq}`]
						),
					]),
				]),
			]),
		];
	}

	private _renderPrivateHome() {
		return w(PrivateHome, this.properties, []);
	}
}
