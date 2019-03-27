import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { State } from '../interfaces';
import CompleteUserInfo, { CompleteUserInfoProperties } from '../pages/user/CompleteUserInfo';
import { loginNameInputProcess, completeUserInfoProcess } from '../processes/userProcesses';
import { ValidateStatus } from '../constant';

function getProperties(store: Store<State>): CompleteUserInfoProperties {
	const { get, path } = store;

	const loginNameErrorMessage = get(path('thirdPartyUser', 'loginNameErrorMessage'));

	return {
		avatarUrl: get(path('thirdPartyUser', 'avatarUrl')),
		nickname: get(path('thirdPartyUser', 'nickname')),
		loginName: get(path('thirdPartyUser', 'loginName')),
		loginNameValidateStatus: !!loginNameErrorMessage
			? ValidateStatus.INVALID
			: get(path('userInputValidation', 'loginNameValidateStatus')),
		loginNameErrorMessage: loginNameErrorMessage || get(path('userInputValidation', 'loginNameErrorMessage')),
		onLoginNameInput: loginNameInputProcess(store),
		onUpdateUserInfo: completeUserInfoProcess(store)
	};
}

export default StoreContainer(CompleteUserInfo, 'state', {
	paths: [['thirdPartyUser'], ['userInputValidation']],
	getProperties
});
