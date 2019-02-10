import { createProcess } from '@dojo/framework/stores/process';
import { replace } from '@dojo/framework/stores/state/operations';
import { commandFactory } from './utils';

const getCurrentUserCommand = commandFactory(async ({ get, path }) => {
	const response = await fetch('/user');
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);
		return [replace(path('user'), {})];
	}

	console.log(response, json);
	return [replace(path('user'), json)];
});

export const getCurrentUserProcess = createProcess('get-current-user', [getCurrentUserCommand]);
