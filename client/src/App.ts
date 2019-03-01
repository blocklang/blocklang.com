import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import Outlet from '@dojo/framework/routing/Outlet';

import HeaderContainer from './containers/HeaderContainer';
import HomeContainer from './containers/HomeContainer';
import NewProjectContainer from './containers/project/NewProjectContainer';
import ViewProjectContainer from './containers/project/ViewProjectContainer';

import About from './widgets/About';
import Profile from './pages/user/Profile';

import { library } from '@fortawesome/fontawesome-svg-core';
import { faGithub } from '@fortawesome/free-brands-svg-icons/faGithub';

import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus';
import { faBookOpen } from '@fortawesome/free-solid-svg-icons/faBookOpen';
import { faEdit } from '@fortawesome/free-solid-svg-icons/faEdit';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons/faInfoCircle';
import { faLock } from '@fortawesome/free-solid-svg-icons/faLock';
import { faSignOutAlt } from '@fortawesome/free-solid-svg-icons/faSignOutAlt';

library.add(faGithub, faPlus, faBookOpen, faEdit, faInfoCircle, faLock, faSignOutAlt);

import * as css from './App.m.css';

export default class App extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root] }, [
			w(HeaderContainer, {}),
			v('div', [
				w(Outlet, { key: 'home', id: 'home', renderer: () => w(HomeContainer, {}) }),
				w(Outlet, { key: 'new-project', id: 'new-project', renderer: () => w(NewProjectContainer, {}) }),
				w(Outlet, { key: 'view-project', id: 'view-project', renderer: () => w(ViewProjectContainer, {}) }),
				w(Outlet, { key: 'about', id: 'about', renderer: () => w(About, {}) }),
				w(Outlet, { key: 'profile', id: 'profile', renderer: () => w(Profile, { username: 'Block Lang' }) })
			])
		]);
	}
}
