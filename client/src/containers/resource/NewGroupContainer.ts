import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewGroup, { NewGroupProperties } from '../../pages/resource/group/NewGroup';
import {
	groupKeyInputProcess,
	groupNameInputProcess,
	groupDescriptionInputProcess,
	saveGroupProcess,
} from '../../processes/repositoryGroupProcesses';

function getProperties(store: Store<State>): NewGroupProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('repository')),
		parentId: get(path('repositoryResource', 'id')),
		parentGroups: get(path('parentGroups')),
		keyValidateStatus: get(path('groupInputValidation', 'keyValidateStatus')),
		keyErrorMessage: get(path('groupInputValidation', 'keyErrorMessage')),
		nameValidateStatus: get(path('groupInputValidation', 'nameValidateStatus')),
		nameErrorMessage: get(path('groupInputValidation', 'nameErrorMessage')),
		saveFailedErrors: get(path('errors')),
		onKeyInput: groupKeyInputProcess(store),
		onNameInput: groupNameInputProcess(store),
		onDescriptionInput: groupDescriptionInputProcess(store),
		onSaveGroup: saveGroupProcess(store),
	};
}

export default StoreContainer(NewGroup, 'state', {
	paths: [['user'], ['repository'], ['repositoryResource'], ['parentGroups'], ['groupInputValidation']],
	getProperties,
});
