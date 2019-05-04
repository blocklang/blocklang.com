import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory, getHeaders } from './utils';
import { replace } from '@dojo/framework/stores/state/operations';
import { baseUrl } from '../config';
import { ValidateStatus } from '../constant';
import { DescriptionPayload, PageKeyPayload, PageNamePayload } from './interfaces';
import { getProjectCommand } from './projectProcesses';

const startInitForNewPageCommand = commandFactory(({ path }) => {
	return [
		replace(path('pageInputValidation', 'keyValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('pageInputValidation', 'keyErrorMessage'), ''),
		replace(path('pageInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('pageInputValidation', 'nameErrorMessage'), '')
	];
});

export const getResourceParentPathCommand = commandFactory(
	async ({ path, payload: { owner, project, parentPath = '' } }) => {
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/group-path/${parentPath}`, {
			headers: getHeaders()
		});
		const json = await response.json();
		if (!response.ok) {
			return [replace(path('parentResource'), undefined)];
		}

		return [
			replace(path('parentResource', 'path'), parentPath),
			replace(path('parentResource', 'id'), json.parentId),
			replace(path('parentResource', 'parentGroups'), json.parentGroups)
		];
	}
);

const getAppTypesCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/properties/app-type`, {
		headers: getHeaders()
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
	async ({ path, payload: { owner, project, parentId, appType, key } }) => {
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
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/pages/check-key`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				key: trimedKey,
				parentId,
				appType
			})
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
	async ({ path, payload: { owner, project, parentId, appType, name } }) => {
		const trimedName = name.trim();
		const result = [];
		// 服务器端校验，校验所属分组下是否存在该 name
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/pages/check-name`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				name: trimedName,
				parentId,
				appType
			})
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

const savePageCommand = commandFactory(async ({ path, get, payload: { owner, project } }) => {
	const pageParam = get(path('pageParam'));

	const parentResource = get(path('parentResource'));
	pageParam.parentId = parentResource.id;

	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/pages`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			...pageParam
		})
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		return [replace(path('errors'), json.errors)];
	}

	return [
		// 清空输入参数
		replace(path('pageParam'), undefined),
		replace(path('routing', 'outlet'), 'view-project'),
		replace(path('routing', 'params'), { owner, project })
	];
});

export const initForNewPageProcess = createProcess('init-for-new-page', [
	startInitForNewPageCommand,
	[getProjectCommand, getResourceParentPathCommand, getAppTypesCommand]
]);
export const pageKeyInputProcess = createProcess('page-key-input', [pageKeyInputCommand]);
export const pageNameInputProcess = createProcess('page-name-input', [pageNameInputCommand]);
export const pageDescriptionInputProcess = createProcess('page-description-input', [pageDescriptionInputCommand]);
export const savePageProcess = createProcess('save-page', [savePageCommand]);
