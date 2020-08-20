import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory, getHeaders, linkTo } from './utils';
import { replace, remove } from '@dojo/framework/stores/state/operations';
import { baseUrl } from '../config';
import { ValidateStatus } from '../constant';
import { DescriptionPayload, PageKeyPayload, PageNamePayload } from './interfaces';
import { getRepositoryCommand } from './repositoryProcesses';

const startInitForNewPageCommand = commandFactory(({ path }) => {
	return [
		replace(path('pageInputValidation', 'keyValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('pageInputValidation', 'keyErrorMessage'), ''),
		replace(path('pageInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('pageInputValidation', 'nameErrorMessage'), ''),
		remove(path('repositoryResource')),
		replace(path('repositoryResource', 'isLoading'), true),
		replace(path('repositoryResource', 'isLoaded'), false),
	];
});

const startInitForViewPageCommand = commandFactory(({ path }) => {
	return [remove(path('repositoryResource')), remove(path('parentGroups'))];
});

export const getResourceParentPathCommand = commandFactory(
	async ({ path, payload: { owner, repo, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/group-path/${parentPath}`, {
			headers: getHeaders(),
		});
		const json = await response.json();
		if (!response.ok) {
			return [remove(path('repositoryResource')), replace(path('parentGroups'), [])];
		}

		return [
			replace(path('repositoryResource', 'id'), json.id),
			replace(path('repositoryResource', 'fullPath'), parentPath),
			replace(path('repositoryResource', 'isLoading'), false),
			replace(path('repositoryResource', 'isLoaded'), true),
			replace(path('parentGroups'), json.parentGroups),
		];
	}
);

const getAppTypesCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/properties/app-type`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('appTypes'), [])];
	}

	const result = [];
	// 初始化 appType 的值
	if (json.length > 0) {
		result.push(replace(path('pageParam', 'appType'), json[0].key));
	}
	result.push(replace(path('appTypes'), json));
	return result;
});

const pageKeyInputCommand = commandFactory<PageKeyPayload>(
	async ({ path, payload: { owner, repo, parentId, appType, key } }) => {
		const trimedKey = key.trim();
		const result = [];

		// 校验是否已填写页面名称
		if (trimedKey === '') {
			result.push(replace(path('pageInputValidation', 'keyValidateStatus'), ValidateStatus.INVALID));
			result.push(replace(path('pageInputValidation', 'keyErrorMessage'), '名称不能为空'));
			return result;
		}

		//校验页面名称是否符合：字母、数字、中划线(-)、下划线(_)
		var regex = /^[a-zA-Z0-9\-\w]+$/g;
		if (!regex.test(trimedKey)) {
			result.push(replace(path('pageInputValidation', 'keyValidateStatus'), ValidateStatus.INVALID));
			result.push(
				replace(path('pageInputValidation', 'keyErrorMessage'), '只允许字母、数字、中划线(-)、下划线(_)')
			);
			return result;
		}

		// 服务器端校验，所属分组下是否存在该 key
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/pages/check-key`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				key: trimedKey,
				parentId,
				appType,
			}),
		});
		const json = await response.json();
		if (!response.ok) {
			result.push(replace(path('pageInputValidation', 'keyValidateStatus'), ValidateStatus.INVALID));
			result.push(replace(path('pageInputValidation', 'keyErrorMessage'), json.errors.key));
			return result;
		}

		// 校验通过
		result.push(replace(path('pageInputValidation', 'keyValidateStatus'), ValidateStatus.VALID));
		result.push(replace(path('pageInputValidation', 'keyErrorMessage'), ''));

		result.push(replace(path('pageParam', 'key'), trimedKey));
		return result;
	}
);

const pageNameInputCommand = commandFactory<PageNamePayload>(
	async ({ path, payload: { owner, repo, parentId, appType, name } }) => {
		const trimedName = name.trim();
		const result = [];
		// 服务器端校验，校验所属分组下是否存在该 name
		const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/pages/check-name`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				name: trimedName,
				parentId,
				appType,
			}),
		});
		const json = await response.json();
		if (!response.ok) {
			result.push(replace(path('pageInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
			result.push(replace(path('pageInputValidation', 'nameErrorMessage'), json.errors.name));
			return result;
		}

		// 校验通过
		result.push(replace(path('pageInputValidation', 'nameValidateStatus'), ValidateStatus.VALID));
		result.push(replace(path('pageInputValidation', 'nameErrorMessage'), ''));

		result.push(replace(path('pageParam', 'name'), trimedName));
		return result;
	}
);

const pageDescriptionInputCommand = commandFactory<DescriptionPayload>(({ path, payload: { description } }) => {
	return [replace(path('pageParam', 'description'), description.trim())];
});

const savePageCommand = commandFactory(async ({ path, get, payload: { owner, repo, parentPath = '' } }) => {
	const pageParam = get(path('pageParam'));

	const projectResource = get(path('repositoryResource'));
	pageParam.parentId = projectResource.id;

	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/pages`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			...pageParam,
		}),
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		return [replace(path('errors'), json.errors)];
	}

	return [
		// 清空输入参数
		replace(path('pageParam'), undefined),
		...linkTo(path, parentPath.length > 0 ? 'view-repo-group' : 'view-repo', { owner, repo, parentPath }),
	];
});

const getPageBaseInfoCommand = commandFactory(async ({ path, get, payload: { owner, repo, pagePath = '' } }) => {
	const response = await fetch(`${baseUrl}/repos/${owner}/${repo}/pages/${pagePath}`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('repositoryResource'), undefined), replace(path('parentGroups'), [])];
	}

	const repositoryResource = json.repositoryResource;
	repositoryResource['fullPath'] = pagePath;

	return [replace(path('repositoryResource'), repositoryResource), replace(path('parentGroups'), json.parentGroups)];
});

export const initForNewPageProcess = createProcess('init-for-new-page', [
	startInitForNewPageCommand,
	[getRepositoryCommand, getResourceParentPathCommand, getAppTypesCommand],
]);
export const pageKeyInputProcess = createProcess('page-key-input', [pageKeyInputCommand]);
export const pageNameInputProcess = createProcess('page-name-input', [pageNameInputCommand]);
export const pageDescriptionInputProcess = createProcess('page-description-input', [pageDescriptionInputCommand]);
export const savePageProcess = createProcess('save-page', [savePageCommand]);
export const initForViewRepositoryPageProcess = createProcess('init-for-view-repository-page', [
	startInitForViewPageCommand,
	getRepositoryCommand,
	getPageBaseInfoCommand,
]);
