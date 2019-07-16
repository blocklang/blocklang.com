import Store from '@dojo/framework/stores/Store';
import { State } from '../../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ListMyComponentRepo, { ListMyComponentRepoProperties } from '../../../pages/user/settings/ListMyComponentRepo';
import { componentRepoUrlInputProcess, publishComponentRepoProcess } from '../../../processes/componentRepoProcess';

function getProperties(store: Store<State>): ListMyComponentRepoProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		publishTasks: get(path('userComponentRepoPublishingTasks')),
		componentRepoInfos: get(path('userComponentRepoInfos')),
		marketplacePageStatusCode: get(path('marketplacePageStatusCode')),
		componentRepoUrl: get(path('componentRepoUrl')),
		repoUrlValidateStatus: get(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus')),
		repoUrlErrorMessage: get(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage')),
		onComponentRepoUrlInput: componentRepoUrlInputProcess(store),
		onPublishComponentRepo: publishComponentRepoProcess(store)
	};
}

export default StoreContainer(ListMyComponentRepo, 'state', {
	paths: [
		['user'],
		['userComponentRepoInfos'],
		['userComponentRepoPublishingTasks'],
		['marketplacePageStatusCode'],
		['componentRepoUrl'],
		['componentRepoUrlInputValidation']
	],
	getProperties
});
