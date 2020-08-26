import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import Route from '@dojo/framework/routing/Route';

import About from './widgets/About';

import 'bootstrap';

import * as css from './App.m.css';
import Exception from './pages/error/Exception';

import HeaderContainer from './containers/HeaderContainer';
import HomeContainer from './containers/HomeContainer';
import NewRepositoryContainer from './containers/repository/NewRepositoryContainer';
import ViewRepositoryContainer from './containers/repository/ViewRepositoryContainer';
import ListReleaseContainer from './containers/release/ListReleaseContainer';
import NewReleaseContainer from './containers/release/NewReleaseContainer';
import ViewDocumentContainer from './containers/help/ViewDocumentContainer';
import ProfileContainer from './containers/user/settings/ProfileContainer';
import CompleteUserInfoContainer from './containers/user/CompleteUserInfoContainer';
import ViewReleaseContainer from './containers/release/ViewReleaseContainer';
import NewPageContainer from './containers/resource/NewPageContainer';
import NewGroupContainer from './containers/resource/NewGroupContainer';
import ViewRepositoryGroupContainer from './containers/repository/ViewRepositoryGroupContainer';
import ListComponentRepoContainer from './containers/marketplace/ListComponentRepoContainer';
import ListMyComponentRepoContainer from './containers/user/settings/ListMyComponentRepoContainer';
import ViewComponentRepoPublishTaskContainer from './containers/user/settings/ViewComponentRepoPublishTaskContainer';
import ViewRepositoryReadmeContainer from './containers/repository/ViewRepositoryReadmeContainer';
import ViewProjectDependenceContainer from './containers/repository/ViewProjectDependenceContainer';
import ViewRepositoryPageContainer from './containers/repository/ViewRepositoryPageContainer';
import ViewRepositoryTempletContainer from './containers/repository/ViewRepositoryTempletContainer';
import ViewRepositoryServiceContainer from './containers/repository/ViewRepositoryServiceContainer';
import FooterContainer from './containers/FooterContainer';
import NewWebProject from './pages/repository/new-web-rpoject';
import NewMiniProgram from './pages/repository/new-mini-program';
import ViewRepositoryBuild from './pages/repository/view-repository-build';
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
					key: 'new-repo',
					id: 'new-repo',
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
					key: 'view-repo',
					id: 'view-repo',
					renderer: () => w(ViewRepositoryContainer, {}),
				}),
				w(Route, {
					key: 'view-repo-group',
					id: 'view-repo-group',
					renderer: () => w(ViewRepositoryGroupContainer, {}),
				}),
				w(Route, {
					key: 'view-repo-readme',
					id: 'view-repo-readme',
					renderer: () => w(ViewRepositoryReadmeContainer, {}),
				}),
				w(Route, {
					key: 'view-repo-build',
					id: 'view-repo-build',
					renderer: (matchDetails) => {
						const { params } = matchDetails;
						return w(ViewRepositoryBuild, { owner: params.owner, repository: params.repository });
					},
				}),
				w(Route, {
					key: 'view-project-dependence',
					id: 'view-project-dependence',
					renderer: () => w(ViewProjectDependenceContainer, {}),
				}),
				w(Route, {
					key: 'view-repo-page',
					id: 'view-repo-page',
					renderer: () => w(ViewRepositoryPageContainer, {}),
				}),
				w(Route, {
					key: 'view-repo-templet',
					id: 'view-repo-templet',
					renderer: () => w(ViewRepositoryTempletContainer, {}),
				}),
				w(Route, {
					key: 'view-repo-service',
					id: 'view-repo-service',
					renderer: () => w(ViewRepositoryServiceContainer, {}),
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
