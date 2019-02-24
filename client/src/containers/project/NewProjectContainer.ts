import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewProject, { NewProjectProperties } from '../../pages/project/NewProject';
import { State } from '../../interfaces';
import {
	nameInputProcess,
	descriptionInputProcess,
	saveProjectProcess,
	isPublicInputProcess
} from '../../processes/projectProcesses';

function getProperties(store: Store<State>): NewProjectProperties {
	const { get, path } = store;
	const projectParam = get(path('projectParam'));
	return {
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl')),
		...projectParam,
		nameValidateStatus: get(path('projectInputValidation', 'nameValidateStatus')),
		nameErrorMessage: get(path('projectInputValidation', 'nameErrorMessage')),

		onNameInput: nameInputProcess(store),
		onDescriptionInput: descriptionInputProcess(store),
		onIsPublicInput: isPublicInputProcess(store),
		onSaveProject: saveProjectProcess(store)
	};
}

export default StoreContainer(NewProject, 'state', {
	paths: [['user'], ['project'], ['projectInputValidation']],
	getProperties
});
