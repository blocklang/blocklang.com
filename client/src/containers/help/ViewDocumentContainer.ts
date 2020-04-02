import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { State } from '../../interfaces';

import ViewDocument, { ViewDocumentProperties } from '../../pages/help/ViewDocument';

function getProperties(store: Store<State>): ViewDocumentProperties {
	const { get, path } = store;
	return {
		content: get(path('help', 'content')),
	};
}

export default StoreContainer(ViewDocument, 'state', {
	paths: [['help']],
	getProperties,
});
