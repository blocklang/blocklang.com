import createStoreMiddleware from '@dojo/framework/core/middleware/store';
import { State } from './interfaces';
import Store from '@dojo/framework/stores/Store';

const store = createStoreMiddleware<State>((store: Store) => {});

export default store;
