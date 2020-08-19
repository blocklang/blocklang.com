import { createProcess } from '@dojo/framework/stores/process';
import { getRepositoryCommand, getLatestCommitInfoCommand } from './repositoryProcesses';
import { commandFactory, getHeaders } from './utils';
import { baseUrl } from '../config';
import { replace, add, remove } from '@dojo/framework/stores/state/operations';
import { ProjectDependenceIdPayload, ProjectDependenceWithProjectPathPayload } from './interfaces';
import { findIndex } from '@dojo/framework/shim/array';

const startInitForViewProjectDependenceCommand = commandFactory(({ path }) => {
	return [
		replace(path('repositoryResource'), undefined),
		replace(path('pagedComponentRepoInfos'), undefined),
		replace(path('projectDependenceResource'), undefined),
	];
});

export const getProjectDependenceResourceCommand = commandFactory(
	async ({ path, payload: { owner, repo, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/dependence`, {
			headers: getHeaders(),
		});
		const json = await response.json();
		if (!response.ok) {
			return [
				replace(path('projectDependenceResource'), undefined),
				replace(path('repositoryResource'), undefined),
			];
		}
		return [
			replace(path('projectDependenceResource'), json),
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

const addDependenceCommand = commandFactory<ProjectDependenceWithProjectPathPayload>(
	async ({ at, get, path, payload: { owner, repo, project, componentRepoId } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/${project}/dependences`, {
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
		return [add(at(path('projectDependenceResource', 'dependences'), 0), json)];
	}
);

const deleteDependenceCommand = commandFactory<ProjectDependenceIdPayload>(
	async ({ at, get, path, payload: { owner, project, id: dependenceId } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${project}/dependences/${dependenceId}`, {
			method: 'DELETE',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		});

		if (response.ok) {
			// 将创建成功后返回的数据插入到数组中
			const dependences = get(path('projectDependenceResource', 'dependences'));
			const index = findIndex(dependences, (item) => item.dependence.id === dependenceId);
			return [remove(at(path('projectDependenceResource', 'dependences'), index))];
		}

		return [];
	}
);

const getProjectDependencesCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/dependences`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('projectDependenceResource', 'dependences'), [])];
	}

	return [replace(path('projectDependenceResource', 'dependences'), json)];
});

const getDependenceVersionsCommand = commandFactory(
	async ({ at, get, path, payload: { dependenceId, componentRepoId } }) => {
		const response = await fetch(`${baseUrl}/component-repos/${componentRepoId}/versions`, {
			headers: getHeaders(),
		});
		const json = await response.json();

		const dependences = get(path('projectDependenceResource', 'dependences'));
		const index = findIndex(dependences, (item) => item.dependence.id === dependenceId);
		const dependencePath = at(path('projectDependenceResource', 'dependences'), index);

		if (!response.ok) {
			// 如果加载出错，则显示错误信息，"获取数据出错，请稍后再试"
			return [
				replace(dependencePath, {
					...dependences[index],
					componentRepoVersions: [],
					loadVersionsErrorMessage: '获取数据出错，请稍后再试',
				}),
			];
		}

		return [
			replace(dependencePath, {
				...dependences[index],
				componentRepoVersions: json,
				loadVersionsErrorMessage: undefined,
			}),
		];
	}
);

const updateDependenceVersionCommand = commandFactory(
	async ({ at, get, path, payload: { owner, project, dependenceId, componentRepoVersionId } }) => {
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/dependences/${dependenceId}`, {
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

		const dependences = get(path('projectDependenceResource', 'dependences'));
		const index = findIndex(dependences, (item) => item.dependence.id === dependenceId);
		const dependencePath = at(path('projectDependenceResource', 'dependences'), index);

		return [replace(dependencePath, { ...dependences[index], componentRepoVersion: json })];
	}
);

export const initForViewProjectDependenceProcess = createProcess('init-for-view-project-dependence', [
	startInitForViewProjectDependenceCommand,
	[getRepositoryCommand, getProjectDependenceResourceCommand, getProjectDependencesCommand],
	getLatestCommitInfoCommand,
]);
export const queryComponentReposForProjectProcess = createProcess('query-component-repos-for-project', [
	getComponentReposCommand,
]);
export const addDependenceProcess = createProcess('add-dependence', [addDependenceCommand]);
export const deleteDependenceProcess = createProcess('delete-dependence', [deleteDependenceCommand]);
export const showDependenceVersionsProcess = createProcess('show-dependence-versions', [getDependenceVersionsCommand]);
export const updateDependenceVersionProcess = createProcess('updateDependenceVersion', [
	updateDependenceVersionCommand,
]);
