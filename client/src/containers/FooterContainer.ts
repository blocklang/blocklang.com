import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { State } from '../interfaces';
import Footer, { FooterProperties } from '../pages/Footer';

function getProperties(store: Store<State>): FooterProperties {
	const { get, path } = store;

	return {
		routing: get(path('routing', 'outlet')),
	};
}

export default StoreContainer(Footer, 'state', { paths: [['routing']], getProperties });
