const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness';
import { w, v } from '@dojo/framework/core/vdom';

import Profile from '../../../src/pages/user/Profile';
import * as css from '../../../src/pages/user/Profile.m.css';

describe('Profile', () => {
	it('default renders correctly', () => {
		const h = harness(() => w(Profile, { username: 'Dojo User' }));
		h.expect(() => v('h1', { classes: [css.root] }, ['Welcome Dojo User!']));
	});
});
