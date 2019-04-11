import { createCommandFactory } from '@dojo/framework/stores/process';
import { State } from '../interfaces';

export function getHeaders(): any {
	const headers: { [key: string]: string } = {
		'X-Requested-With': 'FetchApi'
	};
	return headers;
}

export const commandFactory = createCommandFactory<State>();
