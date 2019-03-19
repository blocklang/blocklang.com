import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import Outlet from '@dojo/framework/routing/Outlet';

import HeaderContainer from './containers/HeaderContainer';
import HomeContainer from './containers/HomeContainer';
import NewProjectContainer from './containers/project/NewProjectContainer';
import ViewProjectContainer from './containers/project/ViewProjectContainer';
import ListReleaseContainer from './containers/release/ListReleaseContainer';
import NewReleaseContainer from './containers/release/NewReleaseContainer';
import ViewDocumentContainer from './containers/help/ViewDocumentContainer';

import About from './widgets/About';
import Profile from './pages/user/Profile';

import 'bootstrap';

import { library } from '@fortawesome/fontawesome-svg-core';

import { faGithub } from '@fortawesome/free-brands-svg-icons/faGithub';
import { faQq } from '@fortawesome/free-brands-svg-icons/faQq';
import { faFirefox } from '@fortawesome/free-brands-svg-icons/faFirefox';
import { faAndroid } from '@fortawesome/free-brands-svg-icons/faAndroid';
import { faApple } from '@fortawesome/free-brands-svg-icons/faApple';
import { faWeixin } from '@fortawesome/free-brands-svg-icons/faWeixin';
import { faAlipay } from '@fortawesome/free-brands-svg-icons/faAlipay';
import { faJava } from '@fortawesome/free-brands-svg-icons/faJava';

import { faFolder } from '@fortawesome/free-solid-svg-icons/faFolder';
import { faSquare } from '@fortawesome/free-solid-svg-icons/faSquare';
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus';
import { faBookOpen } from '@fortawesome/free-solid-svg-icons/faBookOpen';
import { faEdit } from '@fortawesome/free-solid-svg-icons/faEdit';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons/faInfoCircle';
import { faLock } from '@fortawesome/free-solid-svg-icons/faLock';
import { faSignOutAlt } from '@fortawesome/free-solid-svg-icons/faSignOutAlt';
import { faHome } from '@fortawesome/free-solid-svg-icons/faHome';
import { faNewspaper } from '@fortawesome/free-solid-svg-icons/faNewspaper';
import { faPlug } from '@fortawesome/free-solid-svg-icons/faPlug';
import { faTag } from '@fortawesome/free-solid-svg-icons/faTag';
import { faClock } from '@fortawesome/free-solid-svg-icons/faClock';
import { faSpinner } from '@fortawesome/free-solid-svg-icons/faSpinner';
import { faTimes } from '@fortawesome/free-solid-svg-icons/faTimes';
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck';
import { faBan } from '@fortawesome/free-solid-svg-icons/faBan';

library.add(
	faGithub,
	faQq,
	faFirefox,
	faAndroid,
	faApple,
	faWeixin,
	faAlipay,
	faJava,
	faPlus,
	faBookOpen,
	faEdit,
	faInfoCircle,
	faLock,
	faSignOutAlt,
	faHome,
	faFolder,
	faSquare,
	faNewspaper,
	faPlug,
	faTag,
	faClock,
	faSpinner,
	faTimes,
	faCheck,
	faBan
);

import * as css from './App.m.css';

export default class App extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root] }, [
			w(HeaderContainer, {}),
			v('div', [
				w(Outlet, { key: 'home', id: 'home', renderer: () => w(HomeContainer, {}) }),
				w(Outlet, { key: 'new-project', id: 'new-project', renderer: () => w(NewProjectContainer, {}) }),
				w(Outlet, { key: 'view-project', id: 'view-project', renderer: () => w(ViewProjectContainer, {}) }),
				w(Outlet, { key: 'list-release', id: 'list-release', renderer: () => w(ListReleaseContainer, {}) }),
				w(Outlet, { key: 'new-release', id: 'new-release', renderer: () => w(NewReleaseContainer, {}) }),
				w(Outlet, { key: 'docs', id: 'docs', renderer: () => w(ViewDocumentContainer, {}) }),
				w(Outlet, { key: 'about', id: 'about', renderer: () => w(About, {}) }),
				w(Outlet, { key: 'profile', id: 'profile', renderer: () => w(Profile, { username: 'Block Lang' }) })
			])
		]);
	}
}
