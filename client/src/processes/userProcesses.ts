import { createProcess } from '@dojo/framework/stores/process';
import { replace } from '@dojo/framework/stores/state/operations';
import { commandFactory } from './utils';
import { baseUrl } from '../config';
import {
	NicknamePayload,
	WebsiteUrlPayload,
	CompanyPayload,
	LocationPayload,
	BioPayload,
	LoginNamePayload
} from './interfaces';
import { ValidateStatus } from '../constant';

const getCurrentUserCommand = commandFactory(async ({ get, path }) => {
	// 如果用户已存在，则直接返回
	if (get(path('user', 'loginName'))) {
		return [];
	}

	// 否则从服务器端查询
	const response = await fetch(`${baseUrl}/user`);
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('user'), {})];
	}

	// 如果发现是第一次登录，则存到 thirdPartyUser 中；否则存到 user 中。
	if (json.needCompleteUserInfo) {
		return [replace(path('thirdPartyUser'), json), replace(path('routing', 'outlet'), 'complete-user-info')];
	}

	return [replace(path('user'), json)];
});

const getProfileCommand = commandFactory(async ({ path }) => {
	const response = await fetch(`${baseUrl}/user/profile`);
	const json = await response.json();
	if (!response.ok) {
		return [replace(path('profile'), {})];
	}

	return [replace(path('profile'), json)];
});

const updateProfileCommand = commandFactory(async ({ get, path }) => {
	const profile = get(path('profile'));
	const profileParam = get(path('profileParam'));
	const response = await fetch(`${baseUrl}/user/profile`, {
		method: 'PUT',
		headers: { 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			...profile,
			...profileParam
		})
	});

	const json = await response.json();
	if (!response.ok) {
		// TODO: 在页面上提示保存出错
		console.error(response, json);
		return [replace(path('errors'), json.errors), replace(path('profileUpdateSuccessMessage'), undefined)];
	}

	return [replace(path('profile'), json), replace(path('profileUpdateSuccessMessage'), '个人资料修改成功！')];
});

const closeSuccessAlertCommand = commandFactory(({ path }) => {
	return [replace(path('profileUpdateSuccessMessage'), undefined)];
});

const nicknameInputCommand = commandFactory<NicknamePayload>(({ path, payload: { nickname } }) => {
	return [replace(path('profileParam', 'nickname'), nickname.trim())];
});

const websiteUrlInputCommand = commandFactory<WebsiteUrlPayload>(({ path, payload: { websiteUrl } }) => {
	return [replace(path('profileParam', 'websiteUrl'), websiteUrl.trim())];
});

const companyInputCommand = commandFactory<CompanyPayload>(({ path, payload: { company } }) => {
	return [replace(path('profileParam', 'company'), company.trim())];
});

const locationInputCommand = commandFactory<LocationPayload>(({ path, payload: { location } }) => {
	return [replace(path('profileParam', 'location'), location.trim())];
});

const bioInputCommand = commandFactory<BioPayload>(({ path, payload: { bio } }) => {
	return [replace(path('profileParam', 'bio'), bio.trim())];
});

const loginNameInputCommand = commandFactory<LoginNamePayload>(async ({ get, path, payload: { loginName } }) => {
	const trimedLoginName = loginName.trim();
	const result = [];

	// 校验是否已填写项目名称
	if (trimedLoginName === '') {
		return [
			replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID),
			replace(path('userInputValidation', 'loginNameErrorMessage'), '登录名不能为空')
		];
	}

	// 校验项目名称是否符合：字母、数字、中划线(-)、下划线(_)、点(.)
	// 只能以字母或数字开头
	var regex = /^[a-zA-Z0-9]+/g;
	if (!regex.test(trimedLoginName)) {
		return [
			replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID),
			replace(path('userInputValidation', 'loginNameErrorMessage'), '只能以字母或数字开头')
		];
	}
	// 只能以字母或数字结尾
	var regex = /[a-zA-Z0-9]$/g;
	if (!regex.test(trimedLoginName)) {
		return [
			replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID),
			replace(path('userInputValidation', 'loginNameErrorMessage'), '只能以字母或数字结尾')
		];
	}

	// 只能包含字母、数字、下划线(_)或中划线(-)
	var regex = /^[a-zA-Z0-9_-]{1,32}$/g;
	if (!regex.test(trimedLoginName)) {
		return [
			replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID),
			replace(path('userInputValidation', 'loginNameErrorMessage'), '只能包含字母、数字、下划线(_)或中划线(-)')
		];
	}

	// 服务器端校验
	const response = await fetch(`${baseUrl}/user/check-login-name`, {
		method: 'POST',
		headers: { 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			loginName: trimedLoginName
		})
	});
	const json = await response.json();
	if (!response.ok) {
		console.log(response, json);

		result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('userInputValidation', 'loginNameErrorMessage'), json.errors.loginName));
		return result;
	}

	// 校验通过
	return [
		replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.VALID),
		replace(path('userInputValidation', 'loginNameErrorMessage'), undefined),
		replace(path('thirdPartyUser', 'loginNameErrorMessage'), undefined),
		replace(path('thirdPartyUser', 'loginName'), trimedLoginName)
	];
});

const completeUserInfoCommand = commandFactory(async ({ get, path }) => {
	const loginName = get(path('thirdPartyUser', 'loginName')).trim();
	const response = await fetch(`${baseUrl}/user/complete-user-info`, {
		method: 'PUT',
		headers: { 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			loginName
		})
	});

	const json = await response.json();
	if (!response.ok) {
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [
		replace(path('user'), json),
		replace(path('thirdPartyUser'), undefined),
		replace(path('routing', 'outlet'), 'home')
	];
});

export const getCurrentUserProcess = createProcess('get-current-user', [getCurrentUserCommand]);
export const initForUserProfileProcess = createProcess('init-for-user-profile', [getProfileCommand]);
export const nicknameInputProcess = createProcess('nickname-input', [nicknameInputCommand]);
export const websiteUrlInputProcess = createProcess('website-url-input', [websiteUrlInputCommand]);
export const companyInputProcess = createProcess('company-input', [companyInputCommand]);
export const locationInputProcess = createProcess('location-input', [locationInputCommand]);
export const bioInputProcess = createProcess('bio-input', [bioInputCommand]);
export const updateUserProfileProcess = createProcess('update-user-profile', [updateProfileCommand]);
export const closeSuccessAlertProcess = createProcess('close-success-alert', [closeSuccessAlertCommand]);

export const loginNameInputProcess = createProcess('login-name-input', [loginNameInputCommand]);
export const completeUserInfoProcess = createProcess('complete-user-info', [completeUserInfoCommand]);
