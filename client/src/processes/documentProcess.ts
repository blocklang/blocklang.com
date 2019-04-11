import { commandFactory, getHeaders } from './utils';
import { createProcess } from '@dojo/framework/stores/process';
import { replace } from '@dojo/framework/stores/state/operations';
import { baseUrl } from '../config';

const getDocumentCommand = commandFactory(async ({ path, payload: { fileName } }) => {
	const response = await fetch(`${baseUrl}/docs/${fileName}`, {
		headers: getHeaders()
	});
	const docContent = await response.text();
	if (!response.ok) {
		return [replace(path('help', 'content'), '')];
	}

	return [replace(path('help', 'content'), docContent)];
});

export const initForViewDocumentProcess = createProcess('init-for-view-document', [getDocumentCommand]);
