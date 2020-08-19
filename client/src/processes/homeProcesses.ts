import { commandFactory, getHeaders } from './utils';
import { createProcess } from '@dojo/framework/stores/process';
import { replace, remove } from '@dojo/framework/stores/state/operations';
import { baseUrl } from '../config';
import { getCurrentUserCommand } from './userProcesses';

const initCanAccessReposCommand = commandFactory(async ({ get, path }) => {
	// 如果用户未登录，则不获取项目信息
	const isAuthenticated = !!get(path('user', 'loginName'));
	if (!isAuthenticated) {
		return [];
	}

	const response = await fetch(`${baseUrl}/user/repos`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [remove(path('canAccessRepos'))];
	}

	return [replace(path('canAccessRepos'), json)];
});

export const initHomeProcess = createProcess('init-home', [getCurrentUserCommand, initCanAccessReposCommand]);
