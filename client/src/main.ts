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
import { initForNewProjectProcess, initForViewProjectProcess } from './processes/projectProcesses';
import { changeRouteProcess } from './processes/routeProcesses';
import { initHomeProcess } from './processes/homeProcesses';
import {
	initForListReleasesProcess,
	initForNewReleaseProcess,
	initForViewReleaseProcess
} from './processes/releaseProcesses';
import { initForViewDocumentProcess } from './processes/documentProcess';
import { setSessionProcess } from './processes/loginProcesses';

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
				break;
			case 'list-release':
				initForListReleasesProcess(store)({ owner: outlet.params.owner, project: outlet.params.project });
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
// 1. 当每次点击
router.on('nav', ({ outlet, context }: any) => {
	debugger;
	console.log('nav');
	changeRouteProcess(store)({ outlet, context });
});

function onRouteChange() {
	const outlet = store.get(store.path('routing', 'outlet'));
	const params = store.get(store.path('routing', 'params'));

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

// 这样监听，会触发两次，是否可避免重复？
store.onChange(store.path('routing', 'outlet'), onRouteChange);
store.onChange(store.path('routing', 'params'), onRouteChange);

const r = renderer(() => w(App, {}));
r.mount({ registry });
