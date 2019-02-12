import renderer from '@dojo/framework/widget-core/vdom';
import Registry from '@dojo/framework/widget-core/Registry';
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

const store = new Store<State>();
const registry = new Registry();

getCurrentUserProcess(store)({});

registerStoreInjector(store, { registry });
registerRouterInjector(routes, registry, { HistoryManager: StateHistory });
registerThemeInjector(dojo, registry);

const r = renderer(() => w(App, {}));
r.mount({ registry });
