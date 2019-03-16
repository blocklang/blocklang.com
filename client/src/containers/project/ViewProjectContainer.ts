import Store from '@dojo/framework/stores/Store';
import ViewProject, { ViewProjectProperties } from '../../pages/project/ViewProject';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { getUserDeployInfoProcess } from '../../processes/projectProcesses';

function getProperties(store: Store<State>): ViewProjectProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		projectResources: get(path('projectResources')),
		latestCommitInfo: get(path('latestCommitInfo')),
		readme: get(path('readme')),
		userDeployInfo: get(path('userDeployInfo')),
		releaseCount: get(path('releaseCount')),
		onGetDeployInfo: getUserDeployInfoProcess(store)
	};
}

export default StoreContainer(ViewProject, 'state', {
	paths: [['user'], ['project'], ['projectResources'], ['readme'], ['userDeployInfo'], ['releaseCount']],
	getProperties
});
