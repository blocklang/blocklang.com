import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';
import Link from '@dojo/framework/routing/Link';

import * as css from './Header.m.css';

export interface HeaderProperties {
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
}

import messageBundle from '../nls/main';
import { baseUrl } from '../config';

@theme(css)
export default class Header extends ThemedMixin(I18nMixin(WidgetBase))<HeaderProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	// 用户未登录时显示
	private _unauthenticatedMenu() {
		const { messages } = this._localizedMessages;

		return [
			v('li', { key: 'li-0', classes: ['nav-item'] }, [
				v('a', { classes: ['nav-link'], href: `${baseUrl}/oauth2/authorization/github` }, [
					v('i', { classes: ['fab fa-github'] }, []),
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
			v('li', { key: 'li-0', classes: ['nav-item'] }, [
				w(Link, { to: 'new-project', classes: ['nav-link'] }, [
					v('i', { classes: ['fas fa-plus'] }, []),
					` ${messages.newProject}`
				])
			]),
			v('li', { key: 'li-1', classes: ['nav-item'] }, [
				w(Link, { to: `profile`, params: { user: `${loggedUsername}` }, classes: ['nav-link'] }, [
					v('img', { classes: ['avatar'], src: `${loggedAvatarUrl}`, width: 20, height: 20 }, []),
					` ${loggedUsername}`
				])
			]),
			v('li', { key: 'li-2', classes: ['nav-item'] }, [
				v('a', { classes: ['nav-link'], title: `${messages.logout}`, href: `${baseUrl}/logout` }, [
					v('i', { classes: ['fas fa-sign-out-alt'] }, [])
				])
			])
		];
	}

	protected render() {
		const { isAuthenticated } = this.properties;
		const { messages } = this._localizedMessages;

		return v('nav', { classes: [css.root, 'navbar navbar-expand-lg navbar-light bg-light'] }, [
			v('div', { classes: 'container' }, [
				w(Link, { to: 'home', classes: ['navbar-brand'] }, [messages.blockLang]),
				v(
					'ul',
					{ classes: ['navbar-nav ml-auto'] },
					isAuthenticated ? this._authenticatedMenu() : this._unauthenticatedMenu()
				)
			])
		]);
	}
}
