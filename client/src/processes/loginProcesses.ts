import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory } from './utils';
import { SetSessionPayload } from './interfaces';
import { replace } from '@dojo/framework/stores/state/operations';

const setSessionCommand = commandFactory<SetSessionPayload>(({ path, payload: { session } }) => {
	return [replace(path('user'), session)];
});

export const setSessionProcess = createProcess('set-session', [setSessionCommand]);
