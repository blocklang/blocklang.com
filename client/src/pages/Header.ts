import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';
import Link from '@dojo/framework/routing/Link';

import * as c from '../className';
import * as css from './Header.m.css';

export interface HeaderProperties {
	routing: string;
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
}

import messageBundle from '../nls/main';
import { baseUrl } from '../config';
import FontAwesomeIcon from '../widgets/fontawesome-icon';

@theme(css)
export default class Header extends ThemedMixin(I18nMixin(WidgetBase))<HeaderProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	// 用户未登录时显示
	private _unauthenticatedMenu() {
		const { messages } = this._localizedMessages;

		return [
			v('li', { key: 'li-0', classes: [c.nav_item] }, [
				v('a', { classes: [c.nav_link], href: `${baseUrl}/oauth2/authorization/github` }, [
					w(FontAwesomeIcon, { icon: ['fab', 'github'] }),
					` ${messages.loginGithub}`
				])
			])
		];
	}

	// 用户已登录时显示
	private _authenticatedMenu() {
		const { loggedUsername, loggedAvatarUrl } = this.properties;
		const { messages } = this._localizedMessages;

		return [
			v('li', { key: 'li-0', classes: [c.nav_item] }, [
				w(Link, { to: 'new-project', classes: [c.nav_link] }, [
					w(FontAwesomeIcon, { icon: 'plus' }),
					` ${messages.newProject}`
				])
			]),
			v('li', { key: 'li-1', classes: [c.nav_item] }, [
				w(Link, { to: `profile`, params: { user: `${loggedUsername}` }, classes: [c.nav_link] }, [
					v('img', { classes: [c.avatar], src: `${loggedAvatarUrl}`, width: 20, height: 20 }, []),
					` ${loggedUsername}`
				])
			]),
			v('li', { key: 'li-2', classes: [c.nav_item] }, [
				v('a', { classes: [c.nav_link], title: `${messages.logout}`, href: `${baseUrl}/logout` }, [
					w(FontAwesomeIcon, { icon: 'sign-out-alt' })
				])
			])
		];
	}

	protected render() {
		const { isAuthenticated, routing } = this.properties;
		const { messages } = this._localizedMessages;

		const rootClasses = [css.root, c.navbar, c.navbar_expand_lg];
		if (isAuthenticated) {
			rootClasses.push(c.navbar_dark, c.bg_dark);
		} else {
			rootClasses.push(c.navbar_light, c.bg_light);
		}

		let docsMenuActive = false;
		if (routing === 'docs') {
			docsMenuActive = true;
		}

		return v('nav', { classes: rootClasses }, [
			v('div', { classes: [c.container] }, [
				w(Link, { to: 'home', classes: [c.navbar_brand] }, [messages.blockLang]),
				v('ul', { classes: [c.navbar_nav, c.mr_auto] }, [
					v('li', { classes: [c.nav_item] }, [
						w(
							Link,
							{
								classes: [c.nav_link, docsMenuActive ? c.active : null],
								to: 'docs',
								params: { fileName: 'getting-started' }
							},
							['教程']
						)
					])
				]),
				v(
					'ul',
					{ classes: [c.navbar_nav, c.ml_auto] },
					isAuthenticated ? this._authenticatedMenu() : this._unauthenticatedMenu()
				)
			])
		]);
	}
}
