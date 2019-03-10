import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory } from './utils';
import { baseUrl } from '../config';
import { replace } from '@dojo/framework/stores/state/operations';
import { getProjectCommand } from './projectProcesses';

/*******************list releases***********************/
const getProjectReleasesCommand = commandFactory(async ({ path, get, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/releases`);
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);
		return [replace(path('releases'), {})];
	}

	console.log(response, json);
	return [replace(path('releases'), json)];
});

export const initForProjectReleasesProcess = createProcess('init-for-project-releases', [
	// FIXME: 重复请求可能已存在的项目数据？
	// 但如果不获取的话，用户直接刷新路由时就会提示没有项目信息
	// 但用户直接在页面之间通过路由跳转的话，项目信息一直存在的
	getProjectCommand,
	getProjectReleasesCommand
]);
