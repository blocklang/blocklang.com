import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as c from '../className';
import * as css from './Home.m.css';

import PrivateHome from './user/Home';
import messageBundle from '../nls/main';
import { Project } from '../interfaces';
import { baseUrl } from '../config';
import FontAwesomeIcon from '../widgets/fontawesome-icon';

export interface HomeProperties {
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
	canAccessProjects: Project[];
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
		return v('div', { classes: [css.root] }, [
			isAuthenticated ? this._renderPrivateHome() : this._renderPublicHome()
		]);
	}

	private _renderPublicHome() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: [css.jumbotron, c.mt_5, c.jumbotron, c.text_center] }, [
			v('h1', { classes: [] }, [messages.blockLangIntro]),
			v(
				'a',
				{
					classes: [c.btn, c.btn_outline_primary, c.btn_lg, c.my_5],
					href: `${baseUrl}/oauth2/authorization/github`
				},
				[w(FontAwesomeIcon, { icon: ['fab', 'github'], size: 'lg' }), ` ${messages.loginGithub}`]
			)
		]);
	}

	private _renderPrivateHome() {
		return w(PrivateHome, this.properties, []);
	}
}
