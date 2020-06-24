import global from '@dojo/framework/shim/global';
import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory } from './utils';
import { SetSessionPayload } from './interfaces';
import { replace, remove } from '@dojo/framework/stores/state/operations';

const setSessionCommand = commandFactory<SetSessionPayload>(({ path, payload: { session } }) => {
	return [replace(path('user'), session)];
});

const logoutCommand = commandFactory(({ path }) => {
	global.sessionStorage.removeItem('blocklang-session');
	return [remove(path('user'))];
});

export const setSessionProcess = createProcess('set-session', [setSessionCommand]);
export const logoutProcess = createProcess('logout', [logoutCommand]);
