const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness';
import { v, w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';

import Header from '../../../src/widgets/Header';
import * as css from '../../../src/widgets/styles/Header.m.css';

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
								v('i', { classes: ['fab fa-github'] }, []),
								' Github 登录'
							])
						])
					])
				])
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
							w(Link, { to: '/projects/new', classes: ['nav-link'] }, [
								v('i', { classes: ['fas fa-plus'] }, []),
								' 创建项目'
							])
						]),
						v('li', { classes: ['nav-item'] }, [
							w(Link, { to: '/user/jack', classes: ['nav-link'] }, [
								v('img', { classes: [css.avatar], src: '#', width: 20, height: 20 }, []),
								' jack'
							])
						]),
						v('li', { classes: ['nav-item'] }, [
							v('a', { classes: ['nav-link'], title: '退出', href: '/logout' }, [
								v('i', { classes: ['fas fa-sign-out-alt'] }, [])
							])
						])
					])
				])
			])
		);
	});
});
