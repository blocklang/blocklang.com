import Store from '@dojo/framework/stores/Store';
import ViewRepository, { ViewRepositoryProperties } from '../../pages/repository/ViewRepository';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import {
	getUserDeployInfoProcess,
	stageChangesProcess,
	unstageChangesProcess,
	commitChangesProcess,
	commitMessageInputProcess,
} from '../../processes/repositoryProcesses';

function getProperties(store: Store<State>): ViewRepositoryProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		repository: get(path('repository')),
		groupId: get(path('repositoryResource', 'id')),
		path: get(path('repositoryResource', 'fullPath')),
		childResources: get(path('childResources')),

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
		onCommit: commitChangesProcess(store),
	};
}

export default StoreContainer(ViewRepository, 'state', {
	paths: [
		['user'],
		['repository'],
		['repositoryResource'],
		['childResources'],
		['latestCommitInfo'],
		['readme'],
		['userDeployInfo'],
		['releaseCount'],
		['stagedChanges'],
		['unstagedChanges'],
		['commitMessageParam'],
		['commitMessageInputValidation'],
	],
	getProperties,
});
