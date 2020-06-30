import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import Route from '@dojo/framework/routing/Route';

import About from './widgets/About';

import 'bootstrap';

import * as css from './App.m.css';
import Exception from './pages/error/Exception';

import HeaderContainer from './containers/HeaderContainer';
import HomeContainer from './containers/HomeContainer';
import NewRepositoryContainer from './containers/project/NewRepositoryContainer';
import ViewProjectContainer from './containers/project/ViewProjectContainer';
import ListReleaseContainer from './containers/release/ListReleaseContainer';
import NewReleaseContainer from './containers/release/NewReleaseContainer';
import ViewDocumentContainer from './containers/help/ViewDocumentContainer';
import ProfileContainer from './containers/user/settings/ProfileContainer';
import CompleteUserInfoContainer from './containers/user/CompleteUserInfoContainer';
import ViewReleaseContainer from './containers/release/ViewReleaseContainer';
import NewPageContainer from './containers/resource/NewPageContainer';
import NewGroupContainer from './containers/resource/NewGroupContainer';
import ViewProjectGroupContainer from './containers/project/ViewProjectGroupContainer';
import ListComponentRepoContainer from './containers/marketplace/ListComponentRepoContainer';
import ListMyComponentRepoContainer from './containers/user/settings/ListMyComponentRepoContainer';
import ViewComponentRepoPublishTaskContainer from './containers/user/settings/ViewComponentRepoPublishTaskContainer';
import ViewProjectReadmeContainer from './containers/project/ViewProjectReadmeContainer';
import ViewProjectDependenceContainer from './containers/project/ViewProjectDependenceContainer';
import ViewProjectPageContainer from './containers/project/ViewProjectPageContainer';
import ViewProjectTempletContainer from './containers/project/ViewProjectTempletContainer';
import ViewProjectServiceContainer from './containers/project/ViewProjectServiceContainer';
import FooterContainer from './containers/FooterContainer';
import NewWebProject from './pages/project/NewWebProject';
import NewMiniProgram from './pages/project/NewMiniProgram';
import * as icon from './icon';

icon.init();

export default class App extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root] }, [
			w(HeaderContainer, {}),
			v('div', { classes: css.content }, [
				w(Route, { key: 'home', id: 'home', renderer: () => w(HomeContainer, {}) }),
				w(Route, {
					key: 'complete-user-info',
					id: 'complete-user-info',
					renderer: () => w(CompleteUserInfoContainer, {}),
				}),
				w(Route, {
					key: 'new-repository',
					id: 'new-repository',
					renderer: () => w(NewRepositoryContainer, {}),
				}),
				w(Route, {
					key: 'new-project',
					id: 'new-project',
					renderer: (matchDetails) => {
						const { params, queryParams } = matchDetails;
						if (queryParams.type === 'web') {
							return w(NewWebProject, { owner: params.owner, repository: params.repository });
						} else if (queryParams.type === 'miniprogram') {
							return w(NewMiniProgram, { owner: params.owner, repository: params.repository });
						}
					},
				}),
				w(Route, {
					key: 'view-project',
					id: 'view-project',
					renderer: () => w(ViewProjectContainer, {}),
				}),
				w(Route, {
					key: 'view-project-group',
					id: 'view-project-group',
					renderer: () => w(ViewProjectGroupContainer, {}),
				}),
				w(Route, {
					key: 'view-project-readme',
					id: 'view-project-readme',
					renderer: () => w(ViewProjectReadmeContainer, {}),
				}),
				w(Route, {
					key: 'view-project-dependence',
					id: 'view-project-dependence',
					renderer: () => w(ViewProjectDependenceContainer, {}),
				}),
				w(Route, {
					key: 'view-project-page',
					id: 'view-project-page',
					renderer: () => w(ViewProjectPageContainer, {}),
				}),
				w(Route, {
					key: 'view-project-templet',
					id: 'view-project-templet',
					renderer: () => w(ViewProjectTempletContainer, {}),
				}),
				w(Route, {
					key: 'view-project-service',
					id: 'view-project-service',
					renderer: () => w(ViewProjectServiceContainer, {}),
				}),
				w(Route, { key: 'new-page-root', id: 'new-page-root', renderer: () => w(NewPageContainer, {}) }),
				w(Route, { key: 'new-group-root', id: 'new-group-root', renderer: () => w(NewGroupContainer, {}) }),
				w(Route, { key: 'new-page', id: 'new-page', renderer: () => w(NewPageContainer, {}) }),
				w(Route, { key: 'new-group', id: 'new-group', renderer: () => w(NewGroupContainer, {}) }),

				w(Route, { key: 'list-release', id: 'list-release', renderer: () => w(ListReleaseContainer, {}) }),
				w(Route, {
					key: 'new-release',
					id: 'new-release',
					renderer: () => w(NewReleaseContainer, {}),
				}),
				w(Route, { key: 'view-release', id: 'view-release', renderer: () => w(ViewReleaseContainer, {}) }),
				w(Route, { key: 'docs', id: 'docs', renderer: () => w(ViewDocumentContainer, {}) }),
				w(Route, {
					key: 'list-component-repo',
					id: 'list-component-repo',
					renderer: () => w(ListComponentRepoContainer, {}),
				}),
				// 登录用户-setting
				w(Route, {
					key: 'settings-profile',
					id: 'settings-profile',
					renderer: () => w(ProfileContainer, {}),
				}),
				w(Route, {
					key: 'settings-marketplace',
					id: 'settings-marketplace',
					renderer: () => w(ListMyComponentRepoContainer, {}),
				}),
				w(Route, {
					key: 'view-component-repo-publish-task',
					id: 'view-component-repo-publish-task',
					renderer: () => w(ViewComponentRepoPublishTaskContainer, {}),
				}),
				w(Route, { key: 'about', id: 'about', renderer: () => w(About, {}) }),
				w(Route, {
					id: 'errorOutlet',
					renderer: () => {
						return w(Exception, { type: '404' });
					},
				}),
			]),
			w(FooterContainer, {}),
		]);
	}
}
