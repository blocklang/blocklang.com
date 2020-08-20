import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewRepositoryReadme, { ViewRepositoryReadmeProperties } from '../../pages/repository/ViewRepositoryReadme';

function getProperties(store: Store<State>): ViewRepositoryReadmeProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewRepositoryReadme, 'state', {
	paths: [],
	getProperties,
});
