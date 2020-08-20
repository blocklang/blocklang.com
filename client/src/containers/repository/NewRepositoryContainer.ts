import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewRepository, { NewRepositoryProperties } from '../../pages/repository/NewRepository';
import { State } from '../../interfaces';
import {
	nameInputProcess,
	descriptionInputProcess,
	saveRepositoryProcess,
	isPublicInputProcess,
} from '../../processes/repositoryProcesses';

function getProperties(store: Store<State>): NewRepositoryProperties {
	const { get, path } = store;
	const repositoryParam = get(path('repositoryParam'));
	return {
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl')),
		...repositoryParam,
		nameValidateStatus: get(path('repositoryInputValidation', 'nameValidateStatus')),
		nameErrorMessage: get(path('repositoryInputValidation', 'nameErrorMessage')),

		onNameInput: nameInputProcess(store),
		onDescriptionInput: descriptionInputProcess(store),
		onIsPublicInput: isPublicInputProcess(store),
		onSaveRepository: saveRepositoryProcess(store),
	};
}

export default StoreContainer(NewRepository, 'state', {
	paths: [['user'], ['repository'], ['repositoryInputValidation']],
	getProperties,
});
