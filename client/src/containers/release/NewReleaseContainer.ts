import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewRelease, { NewReleaseProperties } from '../../pages/release/NewRelease';
import {
	versionInputProcess,
	titleInputProcess,
	jdkReleaseIdInputProcess,
	descriptionInputProcess,
	saveReleaseTaskProcess,
} from '../../processes/releaseProcesses';

function getProperties(store: Store<State>): NewReleaseProperties {
	const { get, path } = store;

	const projectReleaseParam = get(path('projectReleaseParam'));
	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		jdks: get(path('jdks')),
		...projectReleaseParam,
		versionValidateStatus: get(path('releaseInputValidation', 'versionValidateStatus')),
		versionErrorMessage: get(path('releaseInputValidation', 'versionErrorMessage')),
		titleValidateStatus: get(path('releaseInputValidation', 'titleValidateStatus')),
		titleErrorMessage: get(path('releaseInputValidation', 'titleErrorMessage')),

		onVersionInput: versionInputProcess(store),
		onJdkReleaseIdInput: jdkReleaseIdInputProcess(store),
		onTitleInput: titleInputProcess(store),
		onDescriptionInput: descriptionInputProcess(store),
		onSaveReleaseTask: saveReleaseTaskProcess(store),
	};
}

export default StoreContainer(NewRelease, 'state', {
	paths: [['user'], ['project'], ['jdks'], ['releaseInputValidation']],
	getProperties,
});
