import renderer from '@dojo/framework/core/vdom';
import { w } from '@dojo/framework/core/vdom';
import { Store } from '@dojo/framework/stores/Store';
import { StateHistory } from '@dojo/framework/routing/history/StateHistory';
import { registerRouterInjector } from '@dojo/framework/routing/RouterInjector';
import { registerStoreInjector } from '@dojo/framework/stores/StoreInjector';

import global from '@dojo/framework/shim/global';

import routes from './routes';
import App from './App';
import { State } from './interfaces';
import { initForUserProfileProcess, getCurrentUserProcess } from './processes/userProcesses';
import { initForNewRepositoryProcess } from './processes/repositoryProcesses';
import {
	initForViewRepositoryProcess,
	initForViewRepositoryGroupProcess,
	initForViewCommitChangesProcess,
} from './processes/repositoryProcesses';
import { changeRouteProcess } from './processes/routeProcesses';
import { initHomeProcess } from './processes/homeProcesses';
import {
	initForListReleasesProcess,
	initForNewReleaseProcess,
	initForViewReleaseProcess,
} from './processes/releaseProcesses';
import { initForViewDocumentProcess } from './processes/documentProcess';
import { setSessionProcess } from './processes/loginProcesses';
import { initForNewPageProcess, initForViewRepositoryPageProcess } from './processes/repositoryPageProcesses';
import { initForNewGroupProcess } from './processes/repositoryGroupProcesses';
import {
	initForListComponentReposProcess,
	initForListMyComponentReposProcess,
	initForComponentRepoPublishTask,
} from './processes/componentRepoProcess';
import { initForViewProjectDependenceProcess } from './processes/projectDependenceProcesses';
import { DocumentTitleOptions } from '@dojo/framework/routing/interfaces';

const store = new Store<State>();

// 从 sessionStorage 中获取用户信息
// 刷新浏览器时，不需要再请求用户信息
const userSession = global.sessionStorage.getItem('blocklang-session');
if (userSession) {
	setSessionProcess(store)({ session: JSON.parse(userSession) });
} else {
	// 因为在 homeProcesses 中也调用了获取登录用户信息的方法，出现了在 home 页面都请求两次的情况
	// 所以先注释掉此方法。
	getCurrentUserProcess(store)({});
}

const registry = registerStoreInjector(store);
const router = registerRouterInjector(routes, registry, {
	HistoryManager: StateHistory,
	autostart: false,
	setDocumentTitle,
});

function setDocumentTitle(titleOptions: DocumentTitleOptions): string | undefined {
	const { title, id, params, queryParams } = titleOptions;
	const { get, path } = store;
	if (id === 'profile') {
		// TODO: 不能使用登录信息，使用当前访问的用户信息
		const user = get(path('user'));
		if (user) {
			let result = user.loginName;
			if (user.nickname) {
				result += ` (${user.nickname})`;
			}
			return result;
		}
	}
	if (id === 'view-component-repo-public-task') {
		const { website, owner, repoName } = get(path('componentRepoPublishTask'));
		return `发布组件 · ${website}/${owner}/${repoName}`;
	}
	let repository = get(path('repository'));
	if (!repository) {
		debugger;
		if (params.owner && params.repo) {
			// FIXME: 在这里触发，会导致执行两次初始化请求
			// 这个函数中只支持静态同步设置 title 信息
			initForViewRepositoryProcess(store)({ owner: params.owner, repo: params.repo });
			repository = get(path('repository'));
		}
	}
	if (!repository) {
		return title;
	}
	if (id === 'new-repo') {
		if (queryParams.type === 'web') {
			return '创建 Web 项目';
		}
		if (queryParams.type === 'miniprogram') {
			return '创建小程序';
		}
	}
	if (id === 'view-repo') {
		let result = `${repository.createUserName}/${repository.name}`;
		if (repository.description) {
			result += `: ${repository.description}`;
		}
		return result;
	}
	if (id === 'view-repo-readme') {
		return `${repository.createUserName}/${repository.name}/README`;
	}
	if (id === 'view-project-dependence') {
		const repositoryResource = get(path('repositoryResource'));
		return `${repository.createUserName}/${repository.name}/${repositoryResource.fullPath}DEPENDENCE`;
	}
	if (id === 'view-repo-page') {
		const repositoryResource = get(path('repositoryResource'));
		return `${repository.createUserName}/${repository.name}/${repositoryResource.fullPath}`;
	}
	if (id === 'view-repo-templet') {
	}
	if (id === 'view-repo-service') {
	}
	if (id === 'view-repo-group') {
		const repositoryResource = get(path('repositoryResource'));
		return `${repository.createUserName}/${repository.name}/${repositoryResource.fullPath}`;
	}
	if (id === 'list-release') {
		return `发行版 · ${repository.createUserName}/${repository.name}`;
	}
	if (id === 'new-release') {
		return `创建发行版 · ${repository.createUserName}/${repository.name}`;
	}
	if (id === 'view-release') {
		const projectRelease = get(path('projectRelease'));
		return `${projectRelease.version} · ${repository.createUserName}/${repository.name}`;
	}

	return title;
}

// 当在 outlet 之间切换时，触发该事件
// 可在此处设置 outlet 级别的初始化数据
router.on('outlet', ({ outlet, action }) => {
	if (action === 'enter') {
		let parentPath;

		switch (outlet.id) {
			case 'home':
				// 页面初始化数据
				initHomeProcess(store)({});
				break;
			case 'new-repo':
				initForNewRepositoryProcess(store)({});
				break;
			case 'view-repo':
				debugger;
				initForViewRepositoryProcess(store)({ owner: outlet.params.owner, repo: outlet.params.repo });
				initForViewCommitChangesProcess(store)({ owner: outlet.params.owner, repo: outlet.params.repo });
				break;
			case 'view-repo-group':
				if (outlet.isExact()) {
					parentPath = outlet.params.parentPath;
				} else {
					// 因为 dojo5 route 不支持通配符，所以此处自己实现
					// 注意，pathname 是以 / 开头的
					parentPath = global.window.location.pathname.substring(
						`/${outlet.params.owner}/${outlet.params.project}/groups/`.length
					);
				}
				initForViewRepositoryGroupProcess(store)({
					owner: outlet.params.owner,
					repo: outlet.params.repo,
					parentPath,
				});
				break;
			case 'view-repo-page':
				let pagePath;
				if (outlet.isExact()) {
					pagePath = outlet.params.path;
				} else {
					// 因为 dojo5 route 不支持通配符，所以此处自己实现
					// 注意，pathname 是以 / 开头的
					pagePath = global.window.location.pathname.substring(
						`/${outlet.params.owner}/${outlet.params.repo}/pages/`.length
					);
				}
				initForViewRepositoryPageProcess(store)({
					owner: outlet.params.owner,
					repo: outlet.params.repo,
					pagePath,
				});
				break;
			case 'view-project-dependence':
				if (outlet.isExact()) {
					parentPath = outlet.params.parentPath;
				} else {
					// 因为 dojo5 route 不支持通配符，所以此处自己实现
					// 注意，pathname 是以 / 开头的
					parentPath = global.window.location.pathname.substring(
						`/${outlet.params.owner}/${outlet.params.project}/groups/`.length
					);
				}
				initForViewProjectDependenceProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
					parentPath,
				});
				break;
			case 'list-release':
				initForListReleasesProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
				break;
			case 'new-page-root':
				initForNewPageProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
				});
				break;
			case 'new-page':
				if (outlet.isExact()) {
					parentPath = outlet.params.parentPath;
				} else {
					// 因为 dojo5 route 不支持通配符，所以此处自己实现
					// 注意，pathname 是以 / 开头的
					parentPath = global.window.location.pathname.substring(
						`/${outlet.params.owner}/${outlet.params.project}/pages/new/`.length
					);
				}
				initForNewPageProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
					parentPath,
				});
				break;
			case 'new-group-root':
				initForNewGroupProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
				});
				break;
			case 'new-group':
				if (outlet.isExact()) {
					parentPath = outlet.params.parentPath;
				} else {
					// 因为 dojo5 route 不支持通配符，所以此处自己实现
					// 注意，pathname 是以 / 开头的
					parentPath = global.window.location.pathname.substring(
						`/${outlet.params.owner}/${outlet.params.project}/groups/new/`.length
					);
				}
				initForNewGroupProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
					parentPath,
				});
				break;
			case 'new-release':
				initForNewReleaseProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
				break;
			case 'view-release':
				initForViewReleaseProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
					version: outlet.params.version,
				});
				break;
			case 'list-component-repo':
				initForListComponentReposProcess(store)({});
				break;
			case 'settings-marketplace':
				initForListMyComponentReposProcess(store)({});
				break;
			case 'docs':
				initForViewDocumentProcess(store)({ fileName: outlet.params.fileName });
				break;
			case 'settings-profile':
				initForUserProfileProcess(store)({});
				break;
			case 'view-component-repo-publish-task':
				initForComponentRepoPublishTask(store)({ taskId: outlet.params.taskId });
				break;
		}
	}
});

// 当每次切换 outlet 成功后，都保存起来
// 如何区分是通过 link 切换的，还是通过编程方式切换的
// 如果是通过 link 方式切换的，则不需要再执行 router.setPath
// 如果是通过编程方式切换的，则要执行 router.setPath
router.on('nav', ({ outlet, context }: any) => {
	console.log('nav');
	changeRouteProcess(store)({ outlet, context });
});

function onRouteChange() {
	const outlet = store.get(store.path('routing', 'outlet'));
	const params = store.get(store.path('routing', 'params'));
	const programmatic = store.get(store.path('routing', 'programmatic'));

	if (programmatic === true) {
		if (outlet) {
			const link = router.link(outlet, params);
			if (link === undefined) {
				return;
			}

			// 判断当前路径是否与要设置的路径相同
			// TODO: 这段逻辑应该放在 dojo router 中
			if (link === global.location.pathname) {
				return;
			}
			console.log('onRouteChange', outlet, params);
			router.setPath(link);
		}
	}
}

router.start();

// 这样监听，会触发两次，是否可避免重复？
store.onChange(store.path('routing', 'outlet'), onRouteChange);
store.onChange(store.path('routing', 'params'), onRouteChange);

const r = renderer(() => w(App, {}));
r.mount({ registry });
