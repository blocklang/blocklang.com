import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { State } from '../../../interfaces';
import Profile, { ProfileProperties } from '../../../pages/user/settings/Profile';
import {
	nicknameInputProcess,
	websiteUrlInputProcess,
	companyInputProcess,
	locationInputProcess,
	bioInputProcess,
	updateUserProfileProcess,
	closeSuccessAlertProcess,
} from '../../../processes/userProcesses';

function getProperties(store: Store<State>): ProfileProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		profile: get(path('profile')),
		profileUpdateSuccessMessage: get(path('profileUpdateSuccessMessage')),
		onNicknameInput: nicknameInputProcess(store),
		onWebsiteUrlInput: websiteUrlInputProcess(store),
		onCompanyInput: companyInputProcess(store),
		onLocationInput: locationInputProcess(store),
		onBioInput: bioInputProcess(store),
		onUpdateProfile: updateUserProfileProcess(store),
		onCloseSuccessAlert: closeSuccessAlertProcess(store),
	};
}

export default StoreContainer(Profile, 'state', {
	paths: [['user'], ['profile'], ['profileUpdateSuccessMessage']],
	getProperties,
});
