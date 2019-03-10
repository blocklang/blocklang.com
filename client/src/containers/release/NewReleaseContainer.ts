import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewRelease, { NewReleaseProperties } from '../../pages/release/NewRelease';

function getProperties(store: Store<State>): NewReleaseProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(NewRelease, 'state', {
	paths: [],
	getProperties
});
