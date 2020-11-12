import { replace, remove } from '@dojo/framework/stores/state/operations';
import { linkTo, getHeaders, commandFactory } from './utils';
import { createProcess, ProcessCallback } from '@dojo/framework/stores/process';
import { IsPublicPayload, DescriptionPayload, NamePayload, CommitMessagePayload } from './interfaces';
import { baseUrl } from '../config';
import { ValidateStatus, GitFileStatus } from '../constant';
import { isEmpty } from '../util';
import { UncommittedFile, RepositoryResource } from '../interfaces';

/************************* new repository ****************************/
// 用于设置初始化数据
const startInitForNewRepositoryCommand = commandFactory(({ path }) => {
	return [
		replace(path('repositoryParam', 'isPublic'), true),
		replace(path('repositoryInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('repositoryInputValidation', 'nameErrorMessage'), ''),
	];
});

const nameInputCommand = commandFactory<NamePayload>(async ({ path, get, payload: { name } }) => {
	const userName = get(path('user', 'loginName')); // 确保用户必须登录
	const trimedName = name.trim();
	const result = [];

	// 校验是否已填写项目名称
	if (trimedName === '') {
		result.push(replace(path('repositoryInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('repositoryInputValidation', 'nameErrorMessage'), '仓库名称不能为空'));
		return result;
	}

	// 校验项目名称是否符合：字母、数字、中划线(-)、下划线(_)、点(.)
	var regex = /^[a-zA-Z0-9\-\w\.]+$/g;
	if (!regex.test(trimedName)) {
		result.push(replace(path('repositoryInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(
			replace(
				path('repositoryInputValidation', 'nameErrorMessage'),
				'只允许字母、数字、中划线(-)、下划线(_)、点(.)'
			)
		);
		return result;
	}

	// 服务器端校验，校验登录用户下是否存在该项目名
	const response = await fetch(`${baseUrl}/repos/check-name`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner: userName,
			name: trimedName,
		}),
	});
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);

		result.push(replace(path('repositoryInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('repositoryInputValidation', 'nameErrorMessage'), json.errors.name));
		return result;
	}

	// 校验通过
	result.push(replace(path('repositoryInputValidation', 'nameValidateStatus'), ValidateStatus.VALID));
	result.push(replace(path('repositoryInputValidation', 'nameErrorMessage'), ''));

	result.push(replace(path('repositoryParam', 'name'), trimedName));
	return result;
});

const descriptionInputCommand = commandFactory<DescriptionPayload>(({ path, payload: { description } }) => {
	return [replace(path('repositoryParam', 'description'), description.trim())];
});

const isPublicInputCommand = commandFactory<IsPublicPayload>(({ path, payload: { isPublic } }) => {
	return [replace(path('repositoryParam', 'isPublic'), isPublic)];
});

const saveRepositoryCommand = commandFactory(async ({ path, get }) => {
	const repositoryParam = get(path('repositoryParam'));
	const owner = get(path('user', 'loginName'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/repos`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner,
			...repositoryParam,
		}),
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		replace(path('repository'), json),
		// 清空输入参数
		remove(path('repositoryParam')),
		...linkTo(path, 'view-repo', { owner, repo: repositoryParam.name }),
	];
});

// TODO: 一个字段一个 process vs 一个对象一个 process，哪个更合理？

/************************* view repository ****************************/
export const getRepositoryCommand = commandFactory(async ({ path, payload: { owner, repo } }) => {
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [remove(path('repository'))];
	}

	return [replace(path('repository'), json)];
});

export const getRepositoryGroupInfoCommand = commandFactory(
	async ({ path, payload: { owner, repo, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/groups/${parentPath}`, {
			headers: getHeaders(),
		});
		const json = await response.json();
		if (!response.ok) {
			return [
				replace(path('repositoryResource'), undefined),
				replace(path('parentGroups'), undefined),
				replace(path('childResources'), undefined),
			];
		}

		return [
			replace(path('repositoryResource'), { id: json.id, fullPath: parentPath } as RepositoryResource),
			replace(path('parentGroups'), json.parentGroups),
			replace(path('childResources'), json.childResources),
		];
	}
);

export const getLatestCommitInfoCommand = commandFactory(async ({ get, path, payload: { owner, repo } }) => {
	const repositoryInfo = get(path('repository'));

	if (isEmpty(repositoryInfo)) {
		return [remove(path('latestCommitInfo'))];
	}

	const parentId = get(path('repositoryResource', 'id'));

	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/latest-commit/${parentId}`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [remove(path('latestCommitInfo'))];
	}

	return [replace(path('latestCommitInfo'), json)];
});

const getRepositoryReadmeCommand = commandFactory(async ({ get, path, payload: { owner, repo } }) => {
	const repositoryInfo = get(path('repository'));

	if (isEmpty(repositoryInfo)) {
		return [remove(path('readme'))];
	}

	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/readme`, {
		headers: getHeaders(),
	});
	const readmeContent = await response.text();
	if (!response.ok) {
		return [remove(path('readme'))];
	}

	return [replace(path('readme'), readmeContent)];
});

// const getReleaseCountCommand = commandFactory(async ({ get, path, payload: { owner, repo } }) => {
// 	const repositoryInfo = get(path('repository'));
// 	if (isEmpty(repositoryInfo)) {
// 		return [remove(path('releaseCount'))];
// 	}

// 	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/stats/releases`, {
// 		headers: getHeaders(),
// 	});
// 	const data = await response.json();
// 	if (!response.ok) {
// 		return [replace(path('releaseCount'), undefined)];
// 	}

// 	return [replace(path('releaseCount'), data.total)];
// });

const getDeployInfoCommand = commandFactory(async ({ path, payload: { owner, repo } }) => {
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/deploy_setting`, {
		headers: getHeaders(),
	});
	const userDeployInfo = await response.json();
	if (!response.ok) {
		return [replace(path('userDeployInfo'), undefined)];
	}

	return [replace(path('userDeployInfo'), userDeployInfo)];
});

const getUncommittedFilesCommand = commandFactory(async ({ path, payload: { owner, repo } }) => {
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/changes`, {
		headers: getHeaders(),
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

const stageChangesCommand = commandFactory(async ({ path, payload: { owner, repo, files } }) => {
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/stage-changes`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify(files),
	});

	if (!response.ok) {
		return [];
	}
	return [];
});

const unstageChangesCommand = commandFactory(async ({ payload: { owner, repo, files } }) => {
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/unstage-changes`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify(files),
	});

	if (!response.ok) {
		return [];
	}
	return [];
});

const startInitForViewCommitChangesCommand = commandFactory(({ path }) => {
	return [
		replace(path('commitMessageInputValidation', 'commitMessageValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('commitMessageInputValidation', 'commitMessageErrorMessage'), ''),
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

const commitChangesCommand = commandFactory(async ({ path, get, payload: { owner, repo } }) => {
	const commitMessageParam = get(path('commitMessageParam'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/commits`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify(commitMessageParam),
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		// 清空输入参数
		replace(path('commitMessageParam'), undefined),
	];
});

export const initForNewRepositoryProcess = createProcess('init-for-new-repository', [startInitForNewRepositoryCommand]);
export const nameInputProcess = createProcess('name-input', [nameInputCommand]);
export const descriptionInputProcess = createProcess('description-input', [descriptionInputCommand]);
export const isPublicInputProcess = createProcess('is-public-input', [isPublicInputCommand]);
export const saveRepositoryProcess = createProcess('save-repository', [saveRepositoryCommand]);

export const initForViewRepositoryProcess = createProcess('init-for-view-repository', [
	[getRepositoryCommand, getRepositoryGroupInfoCommand],
	[getLatestCommitInfoCommand, getRepositoryReadmeCommand, getUncommittedFilesCommand],
]);
export const getUserDeployInfoProcess = createProcess('get-user-deploy-info', [getDeployInfoCommand]);

export const initForViewRepositoryGroupProcess = createProcess('init-for-view-repo-group', [
	[getRepositoryCommand, getRepositoryGroupInfoCommand],
	getLatestCommitInfoCommand,
]);

export const getRepositoryProcess = createProcess('get-repository', [getRepositoryCommand]);
export const getRepositoryGroupChildrenProcess = createProcess('get-repository-group-children', [
	getRepositoryGroupInfoCommand,
]);

export const initForViewCommitChangesProcess = createProcess('init-for-view-commit-changes', [
	startInitForViewCommitChangesCommand,
]);

export const stageChangesProcess = createProcess('stage-changes', [stageChangesCommand, getUncommittedFilesCommand]);
export const unstageChangesProcess = createProcess('unstage-changes', [
	unstageChangesCommand,
	getUncommittedFilesCommand,
]);
export const commitMessageInputProcess = createProcess('commit-message-input', [commitMessageInputCommand]);

const reloadRepositoryResourcesAndLatestCommitProcess = createProcess('reload-repository-resources-and-latest-commit', [
	getRepositoryGroupInfoCommand,
	getLatestCommitInfoCommand,
]);

const afterCommit: ProcessCallback = () => ({
	after(error, result) {
		result.executor(reloadRepositoryResourcesAndLatestCommitProcess, { ...result.payload });
	},
});

export const commitChangesProcess = createProcess(
	'commit-changes',
	[commitChangesCommand, getUncommittedFilesCommand],
	[afterCommit]
);
