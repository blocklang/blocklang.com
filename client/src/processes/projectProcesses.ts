import { commandFactory, getHeaders, linkTo } from './utils';
import { NamePayload, DescriptionPayload, IsPublicPayload, CommitMessagePayload } from './interfaces';
import { replace } from '@dojo/framework/stores/state/operations';
import { createProcess, ProcessCallback } from '@dojo/framework/stores/process';
import { ValidateStatus, GitFileStatus } from '../constant';
import { baseUrl } from '../config';
import { isEmpty } from '../util';
import { UncommittedFile } from '../interfaces';

// TODO: 一个字段一个 process vs 一个对象一个 process，哪个更合理？
/************************* new project ****************************/
// 用于设置初始化数据
const startInitForNewProjectCommand = commandFactory(({ path }) => {
	return [
		replace(path('projectParam', 'isPublic'), true),
		replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('projectInputValidation', 'nameErrorMessage'), '')
	];
});

const nameInputCommand = commandFactory<NamePayload>(async ({ path, get, payload: { name } }) => {
	const userName = get(path('user', 'loginName')); // 确保用户必须登录
	const trimedName = name.trim();
	const result = [];

	// 校验是否已填写项目名称
	if (trimedName === '') {
		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('projectInputValidation', 'nameErrorMessage'), '项目名称不能为空'));
		return result;
	}

	// 校验项目名称是否符合：字母、数字、中划线(-)、下划线(_)、点(.)
	var regex = /^[a-zA-Z0-9\-\w\.]+$/g;
	if (!regex.test(trimedName)) {
		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(
			replace(path('projectInputValidation', 'nameErrorMessage'), '只允许字母、数字、中划线(-)、下划线(_)、点(.)')
		);
		return result;
	}

	// 服务器端校验，校验登录用户下是否存在该项目名
	const response = await fetch(`${baseUrl}/projects/check-name`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner: userName,
			name: trimedName
		})
	});
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);

		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('projectInputValidation', 'nameErrorMessage'), json.errors.name));
		return result;
	}

	// 校验通过
	result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.VALID));
	result.push(replace(path('projectInputValidation', 'nameErrorMessage'), ''));

	result.push(replace(path('projectParam', 'name'), trimedName));
	return result;
});

const descriptionInputCommand = commandFactory<DescriptionPayload>(({ path, payload: { description } }) => {
	return [replace(path('projectParam', 'description'), description.trim())];
});

const isPublicInputCommand = commandFactory<IsPublicPayload>(({ path, payload: { isPublic } }) => {
	return [replace(path('projectParam', 'isPublic'), isPublic)];
});

const saveProjectCommand = commandFactory(async ({ path, get }) => {
	const projectParam = get(path('projectParam'));
	const owner = get(path('user', 'loginName'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/projects`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner,
			...projectParam
		})
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		replace(path('project'), json),
		// 清空输入参数
		replace(path('projectParam'), undefined),
		...linkTo(path, 'view-project', { owner, project: projectParam.name })
	];
});

/************************* view project ****************************/
export const getProjectCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('project'), {})];
	}

	return [replace(path('project'), json)];
});

const getProjectResourcesCommand = commandFactory(async ({ path, payload: { owner, project, parentPath = '' } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/groups/${parentPath}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('parentResource'), undefined), replace(path('projectResources'), [])];
	}

	return [
		replace(path('parentResource', 'path'), parentPath),
		replace(path('parentResource', 'id'), json.parentId),
		replace(path('parentResource', 'parentGroups'), json.parentGroups),
		replace(path('projectResources'), json.resources)
	];
});

const getLatestCommitInfoCommand = commandFactory(async ({ get, path, payload: { owner, project } }) => {
	const projectInfo = get(path('project'));

	if (isEmpty(projectInfo)) {
		return [replace(path('latestCommitInfo'), undefined)];
	}

	const parentId = get(path('parentResource', 'id'));

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/latest-commit/${parentId}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('latestCommitInfo'), undefined)];
	}

	return [replace(path('latestCommitInfo'), json)];
});

const getProjectReadmeCommand = commandFactory(async ({ get, path, payload: { owner, project } }) => {
	const projectInfo = get(path('project'));
	console.log(projectInfo);
	if (isEmpty(projectInfo)) {
		return [replace(path('readme'), undefined)];
	}

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/readme`, {
		headers: getHeaders()
	});
	const readmeContent = await response.text();
	if (!response.ok) {
		return [replace(path('readme'), undefined)];
	}

	return [replace(path('readme'), readmeContent)];
});

const getReleaseCountCommand = commandFactory(async ({ get, path, payload: { owner, project } }) => {
	const projectInfo = get(path('project'));
	console.log(projectInfo);
	if (isEmpty(projectInfo)) {
		return [replace(path('releaseCount'), undefined)];
	}

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/stats/releases`, {
		headers: getHeaders()
	});
	const data = await response.json();
	if (!response.ok) {
		return [replace(path('releaseCount'), undefined)];
	}

	return [replace(path('releaseCount'), data.total)];
});

const getDeployInfoCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/deploy_setting`, {
		headers: getHeaders()
	});
	const userDeployInfo = await response.json();
	if (!response.ok) {
		return [replace(path('userDeployInfo'), undefined)];
	}

	return [replace(path('userDeployInfo'), userDeployInfo)];
});

const getUncommittedFilesCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/changes`, {
		headers: getHeaders()
	});
	const jsonData = await response.json();
	if (!response.ok) {
		return [replace(path('stagedChanges'), undefined), replace(path('unstagedChanges'), undefined)];
	}

	const stagedChanges: UncommittedFile[] = [];
	const unstagedChanges: UncommittedFile[] = [];

	jsonData.forEach((item: UncommittedFile) => {
		if (
			item.gitStatus === GitFileStatus.Untracked ||
			item.gitStatus === GitFileStatus.Modified ||
			item.gitStatus === GitFileStatus.Deleted
		) {
			unstagedChanges.push(item);
		} else if (
			item.gitStatus === GitFileStatus.Added ||
			item.gitStatus === GitFileStatus.Changed ||
			item.gitStatus === GitFileStatus.Removed
		) {
			stagedChanges.push(item);
		}
	});

	return [replace(path('stagedChanges'), stagedChanges), replace(path('unstagedChanges'), unstagedChanges)];
});

const stageChangesCommand = commandFactory(async ({ path, payload: { owner, project, files } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/stage-changes`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify(files)
	});

	if (!response.ok) {
		return [];
	}
	return [];
});

const unstageChangesCommand = commandFactory(async ({ payload: { owner, project, files } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/unstage-changes`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify(files)
	});

	if (!response.ok) {
		return [];
	}
	return [];
});

const startInitForViewCommitChangesCommand = commandFactory(({ path }) => {
	return [
		replace(path('commitMessageInputValidation', 'commitMessageValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('commitMessageInputValidation', 'commitMessageErrorMessage'), '')
	];
});

const commitMessageInputCommand = commandFactory<CommitMessagePayload>(({ path, payload: { commitMessage } }) => {
	const trimedCommitMessage = commitMessage.trim();
	const result = [];

	// 校验是否已填写提交信息
	if (trimedCommitMessage === '') {
		result.push(
			replace(path('commitMessageInputValidation', 'commitMessageValidateStatus'), ValidateStatus.INVALID)
		);
		result.push(replace(path('commitMessageInputValidation', 'commitMessageErrorMessage'), '提交信息不能为空'));
		return result;
	}

	// 校验通过
	result.push(replace(path('commitMessageInputValidation', 'commitMessageValidateStatus'), ValidateStatus.VALID));
	result.push(replace(path('commitMessageInputValidation', 'commitMessageErrorMessage'), ''));

	result.push(replace(path('commitMessageParam', 'value'), trimedCommitMessage));
	return result;
});

const commitChangesCommand = commandFactory(async ({ path, get, payload: { owner, project } }) => {
	const commitMessageParam = get(path('commitMessageParam'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/commits`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify(commitMessageParam)
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		// 清空输入参数
		replace(path('commitMessageParam'), undefined)
	];
});

export const initForNewProjectProcess = createProcess('init-for-new-project', [startInitForNewProjectCommand]);
export const nameInputProcess = createProcess('name-input', [nameInputCommand]);
export const descriptionInputProcess = createProcess('description-input', [descriptionInputCommand]);
export const isPublicInputProcess = createProcess('is-public-input', [isPublicInputCommand]);
export const saveProjectProcess = createProcess('save-project', [saveProjectCommand]);

export const initForViewProjectProcess = createProcess('init-for-view-project', [
	[getProjectCommand, getProjectResourcesCommand],
	[getLatestCommitInfoCommand, getProjectReadmeCommand, getReleaseCountCommand, getUncommittedFilesCommand]
]);
export const getUserDeployInfoProcess = createProcess('get-user-deploy-info', [getDeployInfoCommand]);

export const initForViewProjectGroupProcess = createProcess('init-for-view-project-group', [
	[getProjectCommand, getProjectResourcesCommand],
	getLatestCommitInfoCommand
]);

export const initForViewCommitChangesProcess = createProcess('init-for-view-commit-changes', [
	startInitForViewCommitChangesCommand
]);

export const stageChangesProcess = createProcess('stage-changes', [stageChangesCommand, getUncommittedFilesCommand]);
export const unstageChangesProcess = createProcess('unstage-changes', [
	unstageChangesCommand,
	getUncommittedFilesCommand
]);
export const commitMessageInputProcess = createProcess('commit-message-input', [commitMessageInputCommand]);

const reloadProjectResourcesAndLatestCommitProcess = createProcess('reload-project-resources-and-latest-commit', [
	getProjectResourcesCommand,
	getLatestCommitInfoCommand
]);

const afterCommit: ProcessCallback = () => ({
	after(error, result) {
		result.executor(reloadProjectResourcesAndLatestCommitProcess, { ...result.payload });
	}
});

export const commitChangesProcess = createProcess(
	'commit-changes',
	[commitChangesCommand, getUncommittedFilesCommand],
	[afterCommit]
);
