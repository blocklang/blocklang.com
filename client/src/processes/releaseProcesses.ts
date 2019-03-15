import { createProcess } from '@dojo/framework/stores/process';
import { commandFactory } from './utils';
import { baseUrl } from '../config';
import { replace } from '@dojo/framework/stores/state/operations';
import { getProjectCommand } from './projectProcesses';
import { ValidateStatus } from '../constant';
import * as semver from 'semver';

/*******************list releases***********************/
const getProjectReleasesCommand = commandFactory(async ({ path, payload: { owner, project } }) => {
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/releases`);
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);
		return [replace(path('releases'), {})];
	}

	console.log(response, json);
	return [replace(path('releases'), json)];
});

/*******************new release***********************/
const getJdksCommand = commandFactory(async ({ path }) => {
	const response = await fetch(`${baseUrl}/apps/jdk/releases`);
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);
		return [replace(path('jdks'), {})];
	}

	console.log(response, json);
	// 如果列表的大小不为 0， 则将第一项的值设置为 jdkReleaseId 的值
	const result = [];
	if (json.length > 0) {
		result.push(replace(path('projectReleaseParam', 'jdkReleaseId'), json[0].id));
	}
	result.push(replace(path('jdks'), json));
	return result;
});

const versionInputCommand = commandFactory(async ({ path, payload: { owner, project, version } }) => {
	const trimedVersion = version.trim();
	// 版本号不能为空
	if (trimedVersion === '') {
		return [
			replace(path('releaseInputValidation', 'versionValidateStatus'), ValidateStatus.INVALID),
			replace(path('releaseInputValidation', 'versionErrorMessage'), '版本号不能为空')
		];
	}

	// 校验是否有效的语义版本号
	if (semver.valid(trimedVersion) === null) {
		return [
			replace(path('releaseInputValidation', 'versionValidateStatus'), ValidateStatus.INVALID),
			replace(
				path('releaseInputValidation', 'versionErrorMessage'),
				'不是有效的<a href="https://semver.org/lang/zh-CN/" target="_blank">语义化版本</a>'
			)
		];
	}

	// 服务器端校验
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/releases/check-version`, {
		method: 'POST',
		headers: { 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			version: trimedVersion
		})
	});
	const json = await response.json();

	// 服务器端校验未通过
	if (!response.ok) {
		console.log(response, json);
		return [
			replace(path('releaseInputValidation', 'versionValidateStatus'), ValidateStatus.INVALID),
			replace(path('releaseInputValidation', 'versionErrorMessage'), json.errors.version)
		];
	}

	// 校验通过
	return [
		replace(path('releaseInputValidation', 'versionValidateStatus'), ValidateStatus.VALID),
		replace(path('releaseInputValidation', 'versionErrorMessage'), ''),
		replace(path('projectReleaseParam', 'version'), trimedVersion)
	];
});

const jdkReleaseIdInputCommand = commandFactory(({ path, payload: { jdkReleaseId } }) => {
	return [replace(path('projectReleaseParam', 'jdkReleaseId'), jdkReleaseId)];
});

const titleInputCommand = commandFactory(({ path, payload: { title } }) => {
	const trimedTitle = title.trim();
	// 标题不能为空
	if (trimedTitle === '') {
		return [
			replace(path('releaseInputValidation', 'titleValidateStatus'), ValidateStatus.INVALID),
			replace(path('releaseInputValidation', 'titleErrorMessage'), '发行版标题不能为空')
		];
	}
	// 校验通过
	return [
		replace(path('releaseInputValidation', 'titleValidateStatus'), ValidateStatus.VALID),
		replace(path('releaseInputValidation', 'titleErrorMessage'), ''),
		replace(path('projectReleaseParam', 'title'), trimedTitle)
	];
});

const descriptionInputCommand = commandFactory(({ path, payload: { description } }) => {
	return [replace(path('projectReleaseParam', 'description'), description.trim())];
});

const saveReleaseTaskCommand = commandFactory(async ({ path, get, payload: { owner, project } }) => {
	const projectReleaseParam = get(path('projectReleaseParam'));

	// 在跳转到新增项目页面时，应设置 isPublic 的初始值为 true
	const response = await fetch(`${baseUrl}/projects/${owner}/${project}/releases`, {
		method: 'POST',
		headers: { 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			...projectReleaseParam
		})
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		replace(path('projectRelease'), json),
		// 清空输入参数
		replace(path('projectReleaseParam'), undefined),
		replace(path('routing', 'outlet'), 'list-release'),
		replace(path('routing', 'params'), { owner, project })
	];
});

export const initForListReleasesProcess = createProcess('init-for-list-releases', [
	// FIXME: 重复请求可能已存在的项目数据？
	// 但如果不获取的话，用户直接刷新路由时就会提示没有项目信息
	// 但用户直接在页面之间通过路由跳转的话，项目信息一直存在的
	getProjectCommand,
	getProjectReleasesCommand
]);
export const initForNewReleaseProcess = createProcess('init-for-new-release', [getProjectCommand, getJdksCommand]);
export const versionInputProcess = createProcess('version-input', [versionInputCommand]);
export const jdkReleaseIdInputProcess = createProcess('jdk-release-id-input', [jdkReleaseIdInputCommand]);
export const titleInputProcess = createProcess('title-input', [titleInputCommand]);
export const descriptionInputProcess = createProcess('description-input', [descriptionInputCommand]);
export const saveReleaseTaskProcess = createProcess('save-release-task', [saveReleaseTaskCommand]);
