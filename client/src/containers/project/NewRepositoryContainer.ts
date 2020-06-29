import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewRepository, { NewRepositoryProperties } from '../../pages/project/NewRepository';
import { State } from '../../interfaces';
import {
	nameInputProcess,
	descriptionInputProcess,
	saveRepositoryProcess,
	isPublicInputProcess,
} from '../../processes/repositoryProcesses';

function getProperties(store: Store<State>): NewRepositoryProperties {
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
		onSaveRepository: saveRepositoryProcess(store),
	};
}

export default StoreContainer(NewRepository, 'state', {
	paths: [['user'], ['project'], ['projectInputValidation']],
	getProperties,
});
