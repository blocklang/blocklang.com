import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as css from './Home.m.css';

import PrivateHome from './user/Home';
import messageBundle from '../nls/main';
import { Project } from '../interfaces';

export interface HomeProperties {
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
	projects: Project[];
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

		return v('div', { classes: [css.jumbotron, 'jumbotron', 'text-center'] }, [
			v('h1', { classes: [] }, [messages.blockLangIntro]),
			v('a', { classes: ['btn btn-outline-primary btn-lg my-5'], href: '/oauth2/authorization/github' }, [
				v('i', { classes: ['fab fa-github fa-lg'] }),
				` ${messages.loginGithub}`
			])
		]);
	}

	private _renderPrivateHome() {
		return w(PrivateHome, this.properties, []);
	}
}
