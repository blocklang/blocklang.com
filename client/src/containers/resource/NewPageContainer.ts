import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewPage, { NewPageProperties } from '../../pages/resource/page/NewPage';
import {
	pageKeyInputProcess,
	pageDescriptionInputProcess,
	savePageProcess,
	pageNameInputProcess
} from '../../processes/pageProcesses';

function getProperties(store: Store<State>): NewPageProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		appTypes: get(path('appTypes')),
		appType: get(path('pageParam', 'appType')),
		parentId: get(path('parentResource', 'id')),
		keyValidateStatus: get(path('pageInputValidation', 'keyValidateStatus')),
		keyErrorMessage: get(path('pageInputValidation', 'keyErrorMessage')),
		nameValidateStatus: get(path('pageInputValidation', 'nameValidateStatus')),
		nameErrorMessage: get(path('pageInputValidation', 'nameErrorMessage')),
		onKeyInput: pageKeyInputProcess(store),
		onNameInput: pageNameInputProcess(store),
		onDescriptionInput: pageDescriptionInputProcess(store),
		onSavePage: savePageProcess(store)
	};
}

export default StoreContainer(NewPage, 'state', {
	paths: [['user'], ['project'], ['appTypes'], ['pageParam'], ['parentResource'], ['pageInputValidation']],
	getProperties
});
