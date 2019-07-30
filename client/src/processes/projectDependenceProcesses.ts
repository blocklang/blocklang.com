import { createProcess } from '@dojo/framework/stores/process';
import { getProjectCommand, getLatestCommitInfoCommand } from './projectProcesses';
import { commandFactory, getHeaders } from './utils';
import { baseUrl } from '../config';
import { replace } from '@dojo/framework/stores/state/operations';

export const getProjectDependenceCommand = commandFactory(
	async ({ path, payload: { owner, project, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/dependence`, {
			headers: getHeaders()
		});
		const json = await response.json();
		if (!response.ok) {
			return [replace(path('projectDependenceResource'), undefined), replace(path('projectResource'), undefined)];
		}
		return [
			replace(path('projectDependenceResource'), json),
			replace(path('projectResource', 'id'), json.resourceId)
		];
	}
);

export const initForViewProjectDependenceProcess = createProcess('init-for-view-project-dependence', [
	[getProjectCommand, getProjectDependenceCommand],
	getLatestCommitInfoCommand
]);
