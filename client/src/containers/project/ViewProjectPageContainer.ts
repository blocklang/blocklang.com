import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectPage, { ViewProjectPageProperties } from '../../pages/project/ViewProjectPage';
import { initForViewProjectGroupProcess } from '../../processes/projectProcesses';

function getProperties(store: Store<State>): ViewProjectPageProperties {
	const { get, path } = store;

	return {
		loginUser: get(path('user')),
		project: get(path('project')),
		resource: get(path('projectResource')),
		groups: get(path('parentGroups')),
		onGotoGroup: initForViewProjectGroupProcess(store)
	};
}

export default StoreContainer(ViewProjectPage, 'state', {
	paths: [['project'], ['projectResource'], ['parentGroups'], ['user']],
	getProperties
});
