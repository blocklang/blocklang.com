import { createProcess } from '@dojo/framework/stores/process';
import { getRepositoryCommand, getLatestCommitInfoCommand } from './repositoryProcesses';
import { commandFactory, getHeaders } from './utils';
import { baseUrl } from '../config';
import { replace, add, remove } from '@dojo/framework/stores/state/operations';
import { ProjectDependencyIdPayload, ProjectDependencyWithProjectPathPayload } from './interfaces';
import { findIndex } from '@dojo/framework/shim/array';

const startInitForViewProjectDependencyCommand = commandFactory(({ path }) => {
	return [
		replace(path('repositoryResource'), undefined),
		replace(path('pagedComponentRepoInfos'), undefined),
		replace(path('projectDependencyResource'), undefined),
	];
});

export const getProjectDependencyResourceCommand = commandFactory(
	async ({ path, payload: { owner, repo, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/dependency`, {
			headers: getHeaders(),
		});
		const json = await response.json();
		if (!response.ok) {
			return [
				replace(path('projectDependencyResource'), undefined),
				replace(path('repositoryResource'), undefined),
			];
		}
		return [
			replace(path('projectDependencyResource'), json),
			replace(path('repositoryResource', 'id'), json.resourceId),
		];
	}
);

const getComponentReposCommand = commandFactory(async ({ path, payload: { query = '', page = 0 } }) => {
	// 注意，如果 query 的值为空，则返回空列表
	if (query.trim() === '') {
		return [
			replace(path('pagedComponentRepoInfos'), undefined),
			replace(path('marketplacePageStatusCode'), undefined),
		];
	}

	// page 是从 0 开始的
	const response = await fetch(`${baseUrl}/component-repos?q=${query}&page=${page}`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [
			replace(path('pagedComponentRepoInfos'), undefined),
			replace(path('marketplacePageStatusCode'), response.status),
		];
	}

	return [replace(path('pagedComponentRepoInfos'), json)];
});

const addDependencyCommand = commandFactory<ProjectDependencyWithProjectPathPayload>(
	async ({ at, get, path, payload: { owner, repo, project, componentRepoId } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/${project}/dependencies`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				componentRepoId,
			}),
		});

		const json = await response.json();
		if (!response.ok) {
			return [replace(path('errors'), json.errors)];
		}

		// 将创建成功后返回的数据插入到数组中
		return [add(at(path('projectDependencyResource', 'dependencies'), 0), json)];
	}
);

const deleteDependencyCommand = commandFactory<ProjectDependencyIdPayload>(
	async ({ at, get, path, payload: { owner, project, id: dependencyId } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${project}/dependencies/${dependencyId}`, {
			method: 'DELETE',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		});

		if (response.ok) {
			// 将创建成功后返回的数据插入到数组中
			const dependencies = get(path('projectDependencyResource', 'dependencies'));
			const index = findIndex(dependencies, (item) => item.dependency.id === dependencyId);
			return [remove(at(path('projectDependencyResource', 'dependencies'), index))];
		}

		return [];
	}
);

const getProjectDependenciesCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/dependencies`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('projectDependencyResource', 'dependencies'), [])];
	}

	return [replace(path('projectDependencyResource', 'dependencies'), json)];
});

const getDependencyVersionsCommand = commandFactory(
	async ({ at, get, path, payload: { dependencyId, componentRepoId } }) => {
		const response = await fetch(`${baseUrl}/component-repos/${componentRepoId}/versions`, {
			headers: getHeaders(),
		});
		const json = await response.json();

		const dependencies = get(path('projectDependencyResource', 'dependencies'));
		const index = findIndex(dependencies, (item) => item.dependency.id === dependencyId);
		const dependencyPath = at(path('projectDependencyResource', 'dependencies'), index);

		if (!response.ok) {
			// 如果加载出错，则显示错误信息，"获取数据出错，请稍后再试"
			return [
				replace(dependencyPath, {
					...dependencies[index],
					componentRepoVersions: [],
					loadVersionsErrorMessage: '获取数据出错，请稍后再试',
				}),
			];
		}

		return [
			replace(dependencyPath, {
				...dependencies[index],
				componentRepoVersions: json,
				loadVersionsErrorMessage: undefined,
			}),
		];
	}
);

const updateDependencyVersionCommand = commandFactory(
	async ({ at, get, path, payload: { owner, project, dependencyId, componentRepoVersionId } }) => {
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/dependencies/${dependencyId}`, {
			method: 'PUT',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				componentRepoVersionId,
			}),
		});

		const json = await response.json();
		if (!response.ok) {
			// 如果出错，则不更新
			return [];
		}

		const dependencies = get(path('projectDependencyResource', 'dependencies'));
		const index = findIndex(dependencies, (item) => item.dependency.id === dependencyId);
		const dependencyPath = at(path('projectDependencyResource', 'dependencies'), index);

		return [replace(dependencyPath, { ...dependencies[index], componentRepoVersion: json })];
	}
);

export const initForViewProjectDependencyProcess = createProcess('init-for-view-project-dependency', [
	startInitForViewProjectDependencyCommand,
	[getRepositoryCommand, getProjectDependencyResourceCommand, getProjectDependenciesCommand],
	getLatestCommitInfoCommand,
]);
export const queryComponentReposForProjectProcess = createProcess('query-component-repos-for-project', [
	getComponentReposCommand,
]);
export const addDependencyProcess = createProcess('add-dependency', [addDependencyCommand]);
export const deleteDependencyProcess = createProcess('delete-dependency', [deleteDependencyCommand]);
export const showDependencyVersionsProcess = createProcess('show-dependency-versions', [getDependencyVersionsCommand]);
export const updateDependencyVersionProcess = createProcess('update-dependency-version', [
	updateDependencyVersionCommand,
]);
