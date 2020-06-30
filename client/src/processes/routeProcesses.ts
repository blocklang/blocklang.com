import { commandFactory } from './utils';
import { ChangeRoutePayload } from './interfaces';
import { createProcess } from '@dojo/framework/stores/process';
import { replace, remove } from '@dojo/framework/stores/state/operations';

// 也可在此处做 outlet 级别的 teardown 操作
const changeRouteCommand = commandFactory<ChangeRoutePayload>(({ path, payload: { outlet, context } }) => {
	return [
		replace(path('routing', 'programmatic'), false),
		replace(path('routing', 'outlet'), outlet),
		replace(path('routing', 'params'), context.params),
		remove(path('errors')),
		remove(path('projectResource')),
	];
});
export const changeRouteProcess = createProcess('change-route', [changeRouteCommand]);
