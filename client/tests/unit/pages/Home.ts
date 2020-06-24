const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness/harness';
import { w, v } from '@dojo/framework/core/vdom';

import Home from '../../../src/pages/Home';
import * as css from '../../../src/pages/Home.m.css';

import PrivateHome from '../../../src/pages/user/Home';
import FontAwesomeIcon from '../../../src/widgets/fontawesome-icon';

describe('Home', () => {
	it('public home', () => {
		const h = harness(() => w(Home, { isAuthenticated: false, canAccessProjects: [] }));
		h.expect(() =>
			v('div', { classes: [css.root] }, [
				v('div', { classes: [css.jumbotron, 'jumbotron', 'text-center'] }, [
					v('h1', { classes: [] }, ['软件拼装工厂']),
					v('a', { classes: ['btn btn-outline-primary btn-lg my-5'], href: '/oauth2/authorization/github' }, [
						w(FontAwesomeIcon, { icon: ['fab', 'github'], size: 'lg' }),
						' Github 登录',
					]),
				]),
			])
		);
	});

	it('private home', () => {
		const h = harness(() => w(Home, { isAuthenticated: true, loggedUsername: 'jack', canAccessProjects: [] }));
		h.expect(() =>
			v('div', { classes: [css.root] }, [
				w(PrivateHome, { isAuthenticated: true, loggedUsername: 'jack', canAccessProjects: [] }, []),
			])
		);
	});
});
