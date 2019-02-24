import { commandFactory } from './utils';
import { createProcess } from '@dojo/framework/stores/process';
import { replace } from '@dojo/framework/stores/state/operations';

const initCanAccessProjectsCommand = commandFactory(async ({ path }) => {
	const response = await fetch('/user/projects');
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('canAccessProjects'), {})];
	}

	console.log(response, json);
	return [replace(path('canAccessProjects'), json)];
});

export const initPrivateHomeProcess = createProcess('init-private-home', [initCanAccessProjectsCommand]);
