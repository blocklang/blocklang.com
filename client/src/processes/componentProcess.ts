import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory, getHeaders } from './utils';
import { baseUrl } from '../config';
import { replace } from '@dojo/framework/stores/state/operations';

const getComponentsCommand = commandFactory(async ({ path, payload: {} }) => {
	const response = await fetch(`${baseUrl}/components`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('components'), undefined)];
	}

	return [replace(path('components'), json)];
});

export const initForListComponentsProcess = createProcess('init-for-list-component', [getComponentsCommand]);
