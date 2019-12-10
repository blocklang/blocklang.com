import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory, getHeaders, linkTo } from './utils';
import { baseUrl } from '../config';
import { replace } from '@dojo/framework/stores/state/operations';
import { UrlPayload } from './interfaces';
import { ValidateStatus } from '../constant';

const getComponentReposCommand = commandFactory(async ({ path, payload: { query = '', page = 0 } }) => {
	// page 是从 0 开始的
	const response = await fetch(`${baseUrl}/component-repos?q=${query}&page=${page}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [
			replace(path('pagedComponentRepoInfos'), undefined),
			replace(path('marketplacePageStatusCode'), response.status)
		];
	}

	return [replace(path('pagedComponentRepoInfos'), json)];
});

const startInitForNewComponentRepoCommand = commandFactory(({ path }) => {
	return [
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), undefined),
		replace(path('componentRepoUrl'), undefined)
	];
});

const getUserPublishingComponentRepoTasksCommand = commandFactory(async ({ path }) => {
	const response = await fetch(`${baseUrl}/user/component-repos/publishing-tasks`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('userComponentRepoPublishingTasks'), undefined)];
	}

	return [replace(path('userComponentRepoPublishingTasks'), json)];
});

const getUserComponentReposCommand = commandFactory(async ({ path }) => {
	const response = await fetch(`${baseUrl}/user/component-repos`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('userComponentRepoInfos'), undefined)];
	}

	return [replace(path('userComponentRepoInfos'), json)];
});

const getComponentRepoPublishTask = commandFactory(async ({ path, payload: { taskId } }) => {
	const response = await fetch(`${baseUrl}/marketplace/publish/${taskId}`, {
		headers: getHeaders()
	});
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('componentRepoPublishTask'), undefined)];
	}

	return [replace(path('componentRepoPublishTask'), json)];
});

const componentRepoUrlInputCommand = commandFactory<UrlPayload>(({ path, payload: { url } }) => {
	const trimedUrl = url.trim();
	// 校验是否已填写 url
	if (trimedUrl === '') {
		return [
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.INVALID),
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), '不能为空')
		];
	}

	return [
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.VALID),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), undefined),
		replace(path('componentRepoUrl'), trimedUrl)
	];
});

const publishComponentRepoCommand = commandFactory(async ({ path, get }) => {
	const gitUrl = get(path('componentRepoUrl')) || '';

	// 校验是否已填写 url
	if (gitUrl === '') {
		return [
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.INVALID),
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), '不能为空')
		];
	}

	// 服务器端校验，校验登录用户下是否存在该项目名
	const response = await fetch(`${baseUrl}/component-repos`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			gitUrl
		})
	});
	const json = await response.json();
	if (!response.ok) {
		// 目前的处理方式是在界面上显示后台的配置错误
		const errorMessage: string = json.errors.gitUrl || json.errors.propertyConfig;
		return [
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.INVALID),
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), errorMessage)
		];
	}

	// 校验通过
	return [
		replace(path('componentRepoPublishTask'), json),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.VALID),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), undefined),
		// 跳转到发布详情页面
		...linkTo(path, 'view-component-repo-publish-task', { taskId: json.id })
	];
});

export const initForListComponentReposProcess = createProcess('init-for-list-component-repos', [
	getComponentReposCommand
]);
export const queryComponentReposProcess = createProcess('query-component-repos', [getComponentReposCommand]);
export const initForListMyComponentReposProcess = createProcess('init-for-list-my-component-repos', [
	startInitForNewComponentRepoCommand,
	getUserPublishingComponentRepoTasksCommand,
	getUserComponentReposCommand
]);
export const initForComponentRepoPublishTask = createProcess('init-for-component-repo-publish-task', [
	getComponentRepoPublishTask
]);
export const componentRepoUrlInputProcess = createProcess('component-repo-url-input', [componentRepoUrlInputCommand]);
export const publishComponentRepoProcess = createProcess('publish-component-repo', [publishComponentRepoCommand]);
export const getUserComponentReposProcess = createProcess('get-user-component-repos', [getUserComponentReposCommand]);
