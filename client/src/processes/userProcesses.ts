import { createProcess } from '@dojo/framework/stores/process';
import { replace } from '@dojo/framework/stores/state/operations';
import { commandFactory } from './utils';
import { baseUrl } from '../config';

const getCurrentUserCommand = commandFactory(async ({ get, path }) => {
	// 如果用户已存在，则直接返回
	if (get(path('user', 'loginName'))) {
		return [];
	}

	// 否则从服务器端查询
	const response = await fetch(`${baseUrl}/user`);
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('user'), {})];
	}

	return [replace(path('user'), json)];
});

export const getCurrentUserProcess = createProcess('get-current-user', [getCurrentUserCommand]);
