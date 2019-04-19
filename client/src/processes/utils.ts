import global from '@dojo/framework/shim/global';
import { createCommandFactory } from '@dojo/framework/stores/process';
import { State } from '../interfaces';

export function getHeaders(): any {
	const headers: { [key: string]: string } = {
		'X-Requested-With': 'FetchApi'
	};

	const userSession = global.sessionStorage.getItem('blocklang-session');
	if (userSession) {
		headers['X-Token'] = JSON.parse(userSession).token;
	}

	return headers;
}

export const commandFactory = createCommandFactory<State>();
