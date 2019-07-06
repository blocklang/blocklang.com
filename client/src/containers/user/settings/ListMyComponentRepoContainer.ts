import Store from '@dojo/framework/stores/Store';
import { State } from '../../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ListMyComponentRepo, { ListMyComponentRepoProperties } from '../../../pages/user/settings/ListMyComponentRepo';
import { componentRepoUrlInputProcess, publishComponentRepoProcess } from '../../../processes/componentRepoProcess';

function getProperties(store: Store<State>): ListMyComponentRepoProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		pagedComponentRepos: get(path('pagedComponentRepos')),
		marketplacePageStatusCode: get(path('marketplacePageStatusCode')),
		repoUrl: get(path('componentRepoUrl')),
		repoUrlValidateStatus: get(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus')),
		repoUrlErrorMessage: get(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage')),
		repoUrlValidMessage: get(path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage')),
		onComponentRepoUrlInput: componentRepoUrlInputProcess(store),
		onPublishComponentRepo: publishComponentRepoProcess(store)
	};
}

export default StoreContainer(ListMyComponentRepo, 'state', {
	paths: [
		['user'],
		['pagedComponentRepos'],
		['marketplacePageStatusCode'],
		['componentRepoUrl'],
		['componentRepoUrlInputValidation']
	],
	getProperties
});
