import Store from '@dojo/framework/stores/Store';
import ViewProject, { ViewProjectProperties } from '../../pages/project/ViewProject';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import {
	getUserDeployInfoProcess,
	stageChangesProcess,
	unstageChangesProcess,
	commitChangesProcess,
	commitMessageInputProcess
} from '../../processes/projectProcesses';

function getProperties(store: Store<State>): ViewProjectProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		parentId: get(path('parentResource', 'id')),
		parentPath: get(path('parentResource', 'path')),
		projectResources: get(path('projectResources')),
		latestCommitInfo: get(path('latestCommitInfo')),
		readme: get(path('readme')),
		userDeployInfo: get(path('userDeployInfo')),
		releaseCount: get(path('releaseCount')),
		stagedChanges: get(path('stagedChanges')),
		unstagedChanges: get(path('unstagedChanges')),
		commitMessage: get(path('commitMessageParam', 'value')),
		commitMessageValidateStatus: get(path('commitMessageInputValidation', 'commitMessageValidateStatus')),
		commitMessageErrorMessage: get(path('commitMessageInputValidation', 'commitMessageErrorMessage')),
		onGetDeployInfo: getUserDeployInfoProcess(store),
		onStageChanges: stageChangesProcess(store),
		onUnstageChanges: unstageChangesProcess(store),
		onCommitMessageInput: commitMessageInputProcess(store),
		onCommit: commitChangesProcess(store)
	};
}

export default StoreContainer(ViewProject, 'state', {
	paths: [
		['user'],
		['project'],
		['parentResource'],
		['projectResources'],
		['latestCommitInfo'],
		['readme'],
		['userDeployInfo'],
		['releaseCount'],
		['stagedChanges'],
		['unstagedChanges'],
		['commitMessageParam'],
		['commitMessageInputValidation']
	],
	getProperties
});
