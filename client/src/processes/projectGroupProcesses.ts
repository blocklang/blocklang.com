import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory, getHeaders, linkTo } from './utils';
import { replace } from '@dojo/framework/stores/state/operations';
import { ValidateStatus, ResourceType } from '../constant';
import { GroupKeyPayload, GroupNamePayload, DescriptionPayload } from './interfaces';
import { baseUrl } from '../config';
import { getProjectCommand } from './projectProcesses';
import { getResourceParentPathCommand } from './projectPageProcesses';

const startInitForNewGroupCommand = commandFactory(({ path }) => {
	return [
		replace(path('groupInputValidation', 'keyValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('groupInputValidation', 'keyErrorMessage'), ''),
		replace(path('groupInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('groupInputValidation', 'nameErrorMessage'), ''),
		replace(path('projectResource', 'isLoading'), true),
		replace(path('projectResource', 'isLoaded'), false),
	];
});

const groupKeyInputCommand = commandFactory<GroupKeyPayload>(
	async ({ path, payload: { owner, project, parentId, key } }) => {
		const trimedKey = key.trim();
		const result = [];

		// 校验是否已填写页面名称
		if (trimedKey === '') {
			result.push(replace(path('groupInputValidation', 'keyValidateStatus'), ValidateStatus.INVALID));
			result.push(replace(path('groupInputValidation', 'keyErrorMessage'), '名称不能为空'));
			return result;
		}

		//校验页面名称是否符合：字母、数字、中划线(-)、下划线(_)
		var regex = /^[a-zA-Z0-9\-\w]+$/g;
		if (!regex.test(trimedKey)) {
			result.push(replace(path('groupInputValidation', 'keyValidateStatus'), ValidateStatus.INVALID));
			result.push(
				replace(path('groupInputValidation', 'keyErrorMessage'), '只允许字母、数字、中划线(-)、下划线(_)')
			);
			return result;
		}

		// 服务器端校验，所属分组下是否存在该 key
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/groups/check-key`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				key: trimedKey,
				parentId,
			}),
		});
		const json = await response.json();
		if (!response.ok) {
			console.log(response, json);

			result.push(replace(path('groupInputValidation', 'keyValidateStatus'), ValidateStatus.INVALID));
			result.push(replace(path('groupInputValidation', 'keyErrorMessage'), json.errors.key));
			return result;
		}

		// 校验通过
		result.push(replace(path('groupInputValidation', 'keyValidateStatus'), ValidateStatus.VALID));
		result.push(replace(path('groupInputValidation', 'keyErrorMessage'), ''));

		result.push(replace(path('groupParam', 'key'), trimedKey));
		return result;
	}
);

const groupNameInputCommand = commandFactory<GroupNamePayload>(
	async ({ path, payload: { owner, project, parentId, name } }) => {
		const trimedName = name.trim();
		const result = [];
		// 服务器端校验，校验所属分组下是否存在该 name
		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/groups/check-name`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				name: trimedName,
				parentId,
			}),
		});
		const json = await response.json();
		if (!response.ok) {
			console.log(response, json);

			result.push(replace(path('groupInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
			result.push(replace(path('groupInputValidation', 'nameErrorMessage'), json.errors.name));
			return result;
		}

		// 校验通过
		result.push(replace(path('groupInputValidation', 'nameValidateStatus'), ValidateStatus.VALID));
		result.push(replace(path('groupInputValidation', 'nameErrorMessage'), ''));

		result.push(replace(path('groupParam', 'name'), trimedName));
		return result;
	}
);

const groupDescriptionInputCommand = commandFactory<DescriptionPayload>(({ path, payload: { description } }) => {
	return [replace(path('groupParam', 'description'), description.trim())];
});

const saveGroupCommand = commandFactory(
	async ({ path, get, payload: { owner, project, parentPath = '', appType, resourceType = ResourceType.Group } }) => {
		const groupParam = get(path('groupParam'));
		const projectResource = get(path('projectResource'));
		groupParam.parentId = projectResource.id;
		groupParam.appType = appType;
		groupParam.resourceType = resourceType;

		const response = await fetch(`${baseUrl}/projects/${owner}/${project}/groups`, {
			method: 'POST',
			headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
			body: JSON.stringify({
				...groupParam,
			}),
		});

		const json = await response.json();
		if (!response.ok) {
			// TODO: 在页面上提示保存出错
			return [replace(path('errors'), json.errors)];
		}

		return [
			// 清空输入参数
			replace(path('groupParam'), undefined),
			...linkTo(path, parentPath.length > 0 ? 'view-project-group' : 'view-project', {
				owner,
				project,
				parentPath,
			}),
		];
	}
);

export const initForNewGroupProcess = createProcess('init-for-new-group', [
	startInitForNewGroupCommand,
	[getProjectCommand, getResourceParentPathCommand],
]);
export const groupKeyInputProcess = createProcess('group-key-input', [groupKeyInputCommand]);
export const groupNameInputProcess = createProcess('group-name-input', [groupNameInputCommand]);
export const groupDescriptionInputProcess = createProcess('group-description-input', [groupDescriptionInputCommand]);
export const saveGroupProcess = createProcess('save-group', [saveGroupCommand]);
