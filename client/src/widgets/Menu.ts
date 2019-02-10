import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';
// import Toolbar from '@dojo/widgets/toolbar';

import * as css from './styles/Menu.m.css';

export interface MenuProperties {
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
}

export default class Menu extends WidgetBase<MenuProperties> {
	// 用户未登录时显示
	private _unauthenticatedMenu() {
		return [
			v('li', { classes: ['nav-item'] }, [
				v('a', { classes: ['nav-link'], href: '/oauth2/authorization/github' }, [
					v('i', { classes: ['fab fa-github'] }, []),
					' Github 登录'
				])
			])
		];
	}

	// 用户已登录时显示
	private _authenticatedMenu() {
		const { loggedUsername, loggedAvatarUrl } = this.properties;
		return [
			v('li', { classes: ['nav-item'] }, [
				w(Link, { to: `/user/${loggedUsername}`, classes: ['nav-link'] }, [
					v('img', { classes: [css.avatar], src: `${loggedAvatarUrl}`, width: 20, height: 20 }, []),
					` ${loggedUsername}`
				])
			]),
			v('li', { classes: ['nav-item'] }, [
				v('a', { classes: ['nav-link'], title: '退出', href: '/logout' }, [
					v('i', { classes: ['fas fa-sign-out-alt'] }, [])
				])
			])
		];
	}

	protected render() {
		const { isAuthenticated } = this.properties;

		return v('nav', { classes: ['navbar navbar-expand-lg navbar-light bg-light'] }, [
			v('div', { classes: 'container' }, [
				w(Link, { to: 'home', classes: ['navbar-brand'] }, ['Block Lang']),
				v(
					'ul',
					{ classes: ['navbar-nav ml-auto'] },
					isAuthenticated ? this._authenticatedMenu() : this._unauthenticatedMenu()
				)
			])
		]);
	}
}
