import { createProcess } from '@dojo/framework/stores/process';
import { getProjectCommand, getLatestCommitInfoCommand } from './projectProcesses';
import { commandFactory, getHeaders } from './utils';
import { baseUrl } from '../config';
import { replace } from '@dojo/framework/stores/state/operations';

export const getProjectDependenceCommand = commandFactory(
	async ({ path, payload: { owner, project, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/dependence`, {
			headers: getHeaders()
		});
		const json = await response.json();
		if (!response.ok) {
			return [replace(path('projectDependenceResource'), undefined), replace(path('projectResource'), undefined)];
		}
		return [
			replace(path('projectDependenceResource'), json),
			replace(path('projectResource', 'id'), json.resourceId)
		];
	}
);

const getComponentReposCommand = commandFactory(async ({ path, payload: { query = '', page = 0 } }) => {
	// 注意，如果 query 的值为空，则返回空列表
	if (query.trim() === '') {
		return [
			replace(path('pagedComponentRepoInfos'), undefined),
			replace(path('marketplacePageStatusCode'), undefined)
		];
	}

	// page 是从 0 开始的
	const response = await fetch(`${baseUrl}/component-repos?q=${query}&page=${page}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [
			replace(path('pagedComponentRepoInfos'), undefined),
			replace(path('marketplacePageStatusCode'), response.status)
		];
	}

	return [replace(path('pagedComponentRepoInfos'), json)];
});

export const initForViewProjectDependenceProcess = createProcess('init-for-view-project-dependence', [
	[getProjectCommand, getProjectDependenceCommand],
	getLatestCommitInfoCommand
]);

export const queryComponentReposForProjectProcess = createProcess('query-component-repos-for-project', [
	getComponentReposCommand
]);
