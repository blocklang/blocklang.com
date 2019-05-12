import global from '@dojo/framework/shim/global';
import { createCommandFactory } from '@dojo/framework/stores/process';
import { State } from '../interfaces';
import { StatePaths } from '@dojo/framework/stores/Store';
import { Params } from '@dojo/framework/routing/interfaces';
import { replace } from '@dojo/framework/stores/state/operations';

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

export function linkTo(path: StatePaths<State>, outlet: string, params?: Params) {
	const result = [];
	result.push(replace(path('routing', 'programmatic'), true));
	result.push(replace(path('routing', 'outlet'), outlet));
	if (params) {
		result.push(replace(path('routing', 'params'), params));
	}
	return result;
}
