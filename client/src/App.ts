import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import Outlet from '@dojo/framework/routing/Outlet';

import MenuContainer from './containers/MenuContainer';
import HomeContainer from './containers/HomeContainer';
import NewProjectContainer from './containers/project/NewProjectContainer';
import ViewProjectContainer from './containers/project/ViewProjectContainer';

import About from './widgets/About';
import Profile from './widgets/Profile';

import '@fortawesome/fontawesome-free/css/all.min.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import * as css from './App.m.css';

export default class App extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root] }, [
			w(MenuContainer, {}),
			v('div', [
				w(Outlet, { key: 'home', id: 'home', renderer: () => w(HomeContainer, {}) }),
				w(Outlet, { key: 'new-project', id: 'new-project', renderer: () => w(NewProjectContainer, {}) }),
				w(Outlet, { key: 'view-project', id: 'view-project', renderer: () => w(ViewProjectContainer, {}) }),
				w(Outlet, { key: 'about', id: 'about', renderer: () => w(About, {}) }),
				w(Outlet, { key: 'profile', id: 'profile', renderer: () => w(Profile, { username: 'Dojo User' }) })
			])
		]);
	}
}
