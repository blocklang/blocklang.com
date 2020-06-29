import { replace } from '@dojo/framework/stores/state/operations';
import { linkTo, getHeaders, commandFactory } from './utils';
import { createProcess } from '@dojo/framework/stores/process';
import { IsPublicPayload, DescriptionPayload, NamePayload } from './interfaces';
import { baseUrl } from '../config';
import { ValidateStatus } from '../constant';

/************************* new repository ****************************/
// 用于设置初始化数据
const startInitForNewRepositoryCommand = commandFactory(({ path }) => {
	return [
		replace(path('projectParam', 'isPublic'), true),
		replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('projectInputValidation', 'nameErrorMessage'), ''),
	];
});

const nameInputCommand = commandFactory<NamePayload>(async ({ path, get, payload: { name } }) => {
	const userName = get(path('user', 'loginName')); // 确保用户必须登录
	const trimedName = name.trim();
	const result = [];

	// 校验是否已填写项目名称
	if (trimedName === '') {
		result.push(replace(path('projectInputValidation', 'nameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('projectInputValidation', 'nameErrorMessage'), '仓库名称不能为空'));
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
			name: trimedName,
		}),
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

const saveRepositoryCommand = commandFactory(async ({ path, get }) => {
	const projectParam = get(path('projectParam'));
	const owner = get(path('user', 'loginName'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/projects`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			owner,
			...projectParam,
		}),
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
		...linkTo(path, 'view-project', { owner, project: projectParam.name }),
	];
});

export const initForNewRepositoryProcess = createProcess('init-for-new-repository', [startInitForNewRepositoryCommand]);
export const nameInputProcess = createProcess('name-input', [nameInputCommand]);
export const descriptionInputProcess = createProcess('description-input', [descriptionInputCommand]);
export const isPublicInputProcess = createProcess('is-public-input', [isPublicInputCommand]);
export const saveRepositoryProcess = createProcess('save-repository', [saveRepositoryCommand]);
