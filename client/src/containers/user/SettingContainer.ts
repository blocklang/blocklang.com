import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { State } from '../../interfaces';
import Setting, { SettingProperties } from '../../pages/user/Setting';
import {
	nicknameInputProcess,
	websiteUrlInputProcess,
	companyInputProcess,
	locationInputProcess,
	bioInputProcess,
	updateUserProfileProcess,
	closeSuccessAlertProcess
} from '../../processes/userProcesses';

function getProperties(store: Store<State>): SettingProperties {
	const { get, path } = store;

	return {
		profile: get(path('profile')),
		profileUpdateSuccessMessage: get(path('profileUpdateSuccessMessage')),
		onNicknameInput: nicknameInputProcess(store),
		onWebsiteUrlInput: websiteUrlInputProcess(store),
		onCompanyInput: companyInputProcess(store),
		onLocationInput: locationInputProcess(store),
		onBioInput: bioInputProcess(store),
		onUpdateProfile: updateUserProfileProcess(store),
		onCloseSuccessAlert: closeSuccessAlertProcess(store)
	};
}

export default StoreContainer(Setting, 'state', {
	paths: [['profile'], ['profileUpdateSuccessMessage']],
	getProperties
});
