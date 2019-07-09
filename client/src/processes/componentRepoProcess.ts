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
			replace(path('pagedComponentRepos'), undefined),
			replace(path('marketplacePageStatusCode'), response.status)
		];
	}

	return [replace(path('pagedComponentRepos'), json)];
});

const startInitForNewComponentRepoCommand = commandFactory(({ path }) => {
	return [
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.UNVALIDATED),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), undefined),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage'), undefined),
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
		return [replace(path('userComponentRepos'), undefined)];
	}

	return [replace(path('userComponentRepos'), json)];
});

const componentRepoUrlInputCommand = commandFactory<UrlPayload>(({ path, payload: { url } }) => {
	const trimedUrl = url.trim();
	// 校验是否已填写 url
	if (trimedUrl === '') {
		return [
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.INVALID),
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), '不能为空'),
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage'), undefined)
		];
	}

	return [
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.VALID),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), undefined),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage'), undefined),
		replace(path('componentRepoUrl'), trimedUrl)
	];
});

const publishComponentRepoCommand = commandFactory(async ({ path, get }) => {
	const gitUrl = get(path('componentRepoUrl')) || '';
	const result = [];

	// 校验是否已填写 url
	if (gitUrl === '') {
		result.push(
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.INVALID)
		);
		result.push(replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), '不能为空'));
		result.push(replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage'), undefined));
		return result;
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
		console.log(response, json);

		result.push(
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.INVALID)
		);
		result.push(
			replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), json.errors.gitUrl)
		);
		result.push(replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage'), undefined));
		return result;
	}

	// 校验通过
	return [
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlValidateStatus'), ValidateStatus.VALID),
		replace(path('componentRepoUrlInputValidation', 'componentRepoUrlErrorMessage'), undefined),
		replace(
			path('componentRepoUrlInputValidation', 'componentRepoUrlValidMessage'),
			'是有效的 git 远程仓库，开始发布'
		),
		// 跳转到发布详情页面
		...linkTo(path, 'view-component-repo-publish-task', { taskId: json.id })
	];
});

export const initForListComponentReposProcess = createProcess('init-for-list-component-repos', [
	getComponentReposCommand
]);
export const initForListMyComponentReposProcess = createProcess('init-for-list-my-component-repos', [
	startInitForNewComponentRepoCommand,
	getUserPublishingComponentRepoTasksCommand,
	getUserComponentReposCommand
]);
export const componentRepoUrlInputProcess = createProcess('component-repo-url-input', [componentRepoUrlInputCommand]);
export const publishComponentRepoProcess = createProcess('publish-component-repo', [publishComponentRepoCommand]);
