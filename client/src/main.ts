import renderer from '@dojo/framework/widget-core/vdom';
import { w } from '@dojo/framework/widget-core/d';
import { Store } from '@dojo/framework/stores/Store';
import { StateHistory } from '@dojo/framework/routing/history/StateHistory';
import { registerRouterInjector } from '@dojo/framework/routing/RouterInjector';
import { registerThemeInjector } from '@dojo/framework/widget-core/mixins/Themed';
import { registerStoreInjector } from '@dojo/framework/stores/StoreInjector';

import global from '@dojo/framework/shim/global';

import dojo from '@dojo/themes/dojo';
import '@dojo/themes/dojo/index.css';

import routes from './routes';
import App from './App';
import { State } from './interfaces';
import { getCurrentUserProcess, initForUserProfileProcess } from './processes/userProcesses';
import {
	initForNewProjectProcess,
	initForViewProjectProcess,
	initForViewProjectGroupProcess,
	initForViewCommitChangesProcess
} from './processes/projectProcesses';
import { changeRouteProcess } from './processes/routeProcesses';
import { initHomeProcess } from './processes/homeProcesses';
import {
	initForListReleasesProcess,
	initForNewReleaseProcess,
	initForViewReleaseProcess
} from './processes/releaseProcesses';
import { initForViewDocumentProcess } from './processes/documentProcess';
import { setSessionProcess } from './processes/loginProcesses';
import { initForNewPageProcess } from './processes/pageProcesses';
import { initForNewGroupProcess } from './processes/groupProcesses';
import { initForListComponentsProcess } from './processes/componentProcess';

const store = new Store<State>();

// 从 sessionStorage 中获取用户信息
// 刷新浏览器时，不需要再请求用户信息
const userSession = global.sessionStorage.getItem('blocklang-session');
if (userSession) {
	setSessionProcess(store)({ session: JSON.parse(userSession) });
} else {
	getCurrentUserProcess(store)({});
}

const registry = registerStoreInjector(store);
const router = registerRouterInjector(routes, registry, { HistoryManager: StateHistory });
registerThemeInjector(dojo, registry);

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
			case 'new-project':
				initForNewProjectProcess(store)({});
				break;
			case 'view-project':
				initForViewProjectProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
				initForViewCommitChangesProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
				break;
			case 'view-project-group':
				if (outlet.isExact()) {
					parentPath = outlet.params.parentPath;
				} else {
					// 因为 dojo5 route 不支持通配符，所以此处自己实现
					// 注意，pathname 是以 / 开头的
					parentPath = global.window.location.pathname.substring(
						`/${outlet.params.owner}/${outlet.params.project}/groups/`.length
					);
				}
				initForViewProjectGroupProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
					parentPath
				});
				break;
			case 'list-release':
				initForListReleasesProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
				break;
			case 'new-page-root':
				initForNewPageProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project
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
					parentPath
				});
				break;
			case 'new-group-root':
				initForNewGroupProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project
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
					parentPath
				});
				break;
			case 'new-release':
				initForNewReleaseProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
				break;
			case 'view-release':
				initForViewReleaseProcess(store)({
					owner: outlet.params.owner,
					project: outlet.params.project,
					version: outlet.params.version
				});
				break;
			case 'list-component':
				initForListComponentsProcess(store)({});
				break;
			case 'docs':
				initForViewDocumentProcess(store)({ fileName: outlet.params.fileName });
				break;
			case 'settings-profile':
				initForUserProfileProcess(store)({});
				break;
		}
	}
});

// 当每次切换 outlet 成功后，都保存起来
// 如何区分是通过 link 切换的，还是通过编程方式切换的
// 如果是通过 link 方式切换的，则不需要再执行 router.setPath
// 如果是通过编程方式切换的，则要执行 router.setPath
router.on('nav', ({ outlet, context }: any) => {
	debugger;
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

// 这样监听，会触发两次，是否可避免重复？
store.onChange(store.path('routing', 'outlet'), onRouteChange);
store.onChange(store.path('routing', 'params'), onRouteChange);

const r = renderer(() => w(App, {}));
r.mount({ registry });
