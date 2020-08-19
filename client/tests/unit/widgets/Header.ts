const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness/harness';
import { v, w } from '@dojo/framework/core/vdom';
import Link from '@dojo/framework/routing/Link';

import Header from '../../../src/pages/Header';
import * as css from '../../../src/pages/Header.m.css';
import FontAwesomeIcon from '../../../src/widgets/fontawesome-icon';

describe('Header', () => {
	it('user not logged in', () => {
		const h = harness(() => w(Header, { isAuthenticated: false }));
		h.expect(() =>
			v('nav', { classes: [css.root, 'navbar navbar-expand-lg navbar-light bg-light'] }, [
				v('div', { classes: 'container' }, [
					w(Link, { to: 'home', classes: ['navbar-brand'] }, ['Block Lang']),
					v('ul', { classes: ['navbar-nav ml-auto'] }, [
						v('li', { classes: ['nav-item'] }, [
							v('a', { classes: ['nav-link'], href: '/oauth2/authorization/github' }, [
								w(FontAwesomeIcon, { icon: ['fab', 'github'] }),
								' Github 登录',
							]),
						]),
					]),
				]),
			])
		);
	});

	it('user logged in success', () => {
		const h = harness(() => w(Header, { isAuthenticated: true, loggedUsername: 'jack', loggedAvatarUrl: '#' }));
		h.expect(() =>
			v('nav', { classes: [css.root, 'navbar navbar-expand-lg navbar-light bg-light'] }, [
				v('div', { classes: 'container' }, [
					w(Link, { to: 'home', classes: ['navbar-brand'] }, ['Block Lang']),
					v('ul', { classes: ['navbar-nav ml-auto'] }, [
						v('li', { classes: ['nav-item'] }, [
							w(Link, { to: 'new-repo', classes: ['nav-link'] }, [
								w(FontAwesomeIcon, { icon: 'plus' }),
								' 创建仓库',
							]),
						]),
						v('li', { classes: ['nav-item'] }, [
							w(Link, { to: '/user/jack', classes: ['nav-link'] }, [
								v('img', { classes: ['avatar'], src: '#', width: 20, height: 20 }, []),
								' jack',
							]),
						]),
						v('li', { classes: ['nav-item'] }, [
							v('a', { classes: ['nav-link'], title: '退出', href: '/logout' }, [
								w(FontAwesomeIcon, { icon: 'sign-out-alt' }),
							]),
						]),
					]),
				]),
			])
		);
	});
});
