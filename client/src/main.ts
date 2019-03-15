import renderer from '@dojo/framework/widget-core/vdom';
import { w } from '@dojo/framework/widget-core/d';
import { Store } from '@dojo/framework/stores/Store';
import { StateHistory } from '@dojo/framework/routing/history/StateHistory';
import { registerRouterInjector } from '@dojo/framework/routing/RouterInjector';
import { registerThemeInjector } from '@dojo/framework/widget-core/mixins/Themed';
import { registerStoreInjector } from '@dojo/framework/stores/StoreInjector';

import dojo from '@dojo/themes/dojo';
import '@dojo/themes/dojo/index.css';

import routes from './routes';
import App from './App';
import { State } from './interfaces';
import { getCurrentUserProcess } from './processes/userProcesses';
import { initForNewProjectProcess, initForViewProjectProcess } from './processes/projectProcesses';
import { changeRouteProcess } from './processes/routeProcesses';
import { initPrivateHomeProcess } from './processes/homeProcesses';
import { initForListReleasesProcess, initForNewReleaseProcess } from './processes/releaseProcesses';

const store = new Store<State>();

getCurrentUserProcess(store)({});

const registry = registerStoreInjector(store);
const router = registerRouterInjector(routes, registry, { HistoryManager: StateHistory });
registerThemeInjector(dojo, registry);

// 当在 outlet 之间切换时，触发该事件
// 可在此处设置 outlet 级别的初始化数据
router.on('outlet', ({ outlet, action }) => {
	if (action === 'enter') {
		switch (outlet.id) {
			case 'home':
				// const isAuthenticated = !!store.get(store.path('user', 'loginName'));
				getCurrentUserProcess(store)({}).then(function() {
					const isAuthenticated = !!store.get(store.path('user', 'loginName'));
					if (isAuthenticated) {
						initPrivateHomeProcess(store)({});
					}
				});

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
		}
	}
});

// 当每次切换 outlet 成功后，都保存起来
router.on('nav', ({ outlet, context }: any) => {
	console.log('nav');
	changeRouteProcess(store)({ outlet, context });
});

function onRouteChange() {
	console.log('onRouteChange');
	const outlet = store.get(store.path('routing', 'outlet'));
	const params = store.get(store.path('routing', 'params'));

	if (outlet) {
		const link = router.link(outlet, params);
		if (link !== undefined) {
			router.setPath(link);
		}
	}
}

// 这样监听，会触发两次，是否可避免重复？
store.onChange(store.path('routing', 'outlet'), onRouteChange);
store.onChange(store.path('routing', 'params'), onRouteChange);

const r = renderer(() => w(App, {}));
r.mount({ registry });
