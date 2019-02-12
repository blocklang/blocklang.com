import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';

import * as css from './styles/Home.m.css';

import PrivateHome from './user/Home';

export interface HomeProperties {
	isAuthenticated: boolean;
	loggedUsername?: string;
	loggedAvatarUrl?: string;
}

/**
 * 首页
 *
 * 根据用户是否登录，首页分为公共首页和个人首页。
 * 当用户未登录时，显示公共首页；当用户已登录时，在个人首页显示登录用户相关信息。
 */
export default class Home extends WidgetBase<HomeProperties> {
	protected render() {
		const { isAuthenticated } = this.properties;
		return v('div', { classes: [css.root] }, [
			isAuthenticated ? this._renderPrivateHome() : this._renderPublicHome()
		]);
	}

	private _renderPublicHome() {
		return v('div', { classes: [css.jumbotron, 'jumbotron', 'text-center'] }, [
			v('h1', { classes: [] }, ['软件拼装工厂']),
			v('a', { classes: ['btn btn-outline-primary btn-lg my-5'], href: '/oauth2/authorization/github' }, [
				v('i', { classes: ['fab fa-github fa-lg'] }),
				' Github 登录'
			])
		]);
	}

	private _renderPrivateHome() {
		return w(PrivateHome, this.properties, []);
	}
}
