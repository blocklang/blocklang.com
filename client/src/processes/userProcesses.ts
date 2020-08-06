import global from '@dojo/framework/shim/global';
import { createProcess } from '@dojo/framework/stores/process';
import { replace, remove } from '@dojo/framework/stores/state/operations';
import { commandFactory, getHeaders, linkTo } from './utils';
import { baseUrl } from '../config';
import {
	NicknamePayload,
	WebsiteUrlPayload,
	CompanyPayload,
	LocationPayload,
	BioPayload,
	LoginNamePayload,
} from './interfaces';
import { ValidateStatus, LoginStatus } from '../constant';

/**
 * 获取登录用户信息
 */
export const getCurrentUserCommand = commandFactory(async ({ get, path }) => {
	const response = await fetch(`${baseUrl}/user`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		// 没有获取到登录用户信息，则清空缓存的用户信息
		global.sessionStorage.removeItem('blocklang-session');
		return [remove(path('user'))];
	}

	const status = json.status;

	// 如果发现是第一次登录，则存到 thirdPartyUser 中；否则存到 user 中。
	if (LoginStatus.NEED_COMPLETE_USER_INFO === status) {
		// 此时用户并未成功登录，清空缓存的用户信息
		global.sessionStorage.removeItem('blocklang-session');
		return [replace(path('thirdPartyUser'), json), ...linkTo(path, 'complete-user-info')];
	}

	if (LoginStatus.LOGINED === status) {
		global.sessionStorage.setItem('blocklang-session', JSON.stringify(json));
		return [replace(path('user'), json)];
	}

	if (LoginStatus.NOT_LOGIN === status || LoginStatus.FAILED === status) {
		global.sessionStorage.removeItem('blocklang-session');
		return [replace(path('user'), json)];
	}
});

const getProfileCommand = commandFactory(async ({ path }) => {
	const response = await fetch(`${baseUrl}/user/profile`, {
		headers: getHeaders(),
	});
	const json = await response.json();
	if (!response.ok) {
		return [remove(path('profile'))];
	}

	return [replace(path('profile'), json)];
});

const updateProfileCommand = commandFactory(async ({ get, path }) => {
	const profile = get(path('profile'));
	const profileParam = get(path('profileParam'));
	const response = await fetch(`${baseUrl}/user/profile`, {
		method: 'PUT',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			...profile,
			...profileParam,
		}),
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

const loginNameInputCommand = commandFactory<LoginNamePayload>(async ({ path, payload: { loginName } }) => {
	const trimedLoginName = loginName.trim();
	const result = [];

	result.push(replace(path('thirdPartyUser', 'loginName'), trimedLoginName));

	// 校验是否已填写登录名
	if (trimedLoginName === '') {
		result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('userInputValidation', 'loginNameErrorMessage'), '登录名不能为空'));
		return result;
	}

	// 只能以字母或数字开头
	var regex = /^[a-zA-Z0-9]+/g;
	if (!regex.test(trimedLoginName)) {
		result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('userInputValidation', 'loginNameErrorMessage'), '只能以字母或数字开头'));
		return result;
	}
	// 只能以字母或数字结尾
	var regex = /[a-zA-Z0-9]$/g;
	if (!regex.test(trimedLoginName)) {
		result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('userInputValidation', 'loginNameErrorMessage'), '只能以字母或数字结尾'));
		return result;
	}

	// 只能包含字母、数字、下划线(_)或中划线(-)
	var regex = /^[a-zA-Z0-9_-]{1,32}$/g;
	if (!regex.test(trimedLoginName)) {
		result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID));
		result.push(
			replace(path('userInputValidation', 'loginNameErrorMessage'), '只能包含字母、数字、下划线(_)或中划线(-)')
		);
		return result;
	}

	// 服务器端校验
	const response = await fetch(`${baseUrl}/user/check-login-name`, {
		method: 'POST',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			loginName: trimedLoginName,
		}),
	});
	const json = await response.json();
	if (!response.ok) {
		result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.INVALID));
		result.push(replace(path('userInputValidation', 'loginNameErrorMessage'), json.errors.loginName));
		return result;
	}

	// 校验通过
	result.push(replace(path('userInputValidation', 'loginNameValidateStatus'), ValidateStatus.VALID));
	result.push(replace(path('userInputValidation', 'loginNameErrorMessage'), undefined));
	result.push(replace(path('thirdPartyUser', 'loginNameErrorMessage'), undefined));

	return result;
});

const completeUserInfoCommand = commandFactory(async ({ get, path }) => {
	const loginName = get(path('thirdPartyUser', 'loginName')).trim();
	const response = await fetch(`${baseUrl}/user/complete-user-info`, {
		method: 'PUT',
		headers: { ...getHeaders(), 'Content-type': 'application/json;charset=UTF-8' },
		body: JSON.stringify({
			loginName,
		}),
	});

	const json = await response.json();

	// 如果找不到第三方用户信息，则跳转到首页，而不能继续停留在完善用户信息页面
	if (response.status === 403) {
		return [
			replace(path('errors'), undefined),
			replace(path('user'), json),
			replace(path('thirdPartyUser'), undefined),
			...linkTo(path, 'home'),
		];
	}

	if (!response.ok) {
		console.error(response, json);
		return [replace(path('errors'), json.errors)];
	}

	return [replace(path('user'), json), replace(path('thirdPartyUser'), undefined), ...linkTo(path, 'home')];
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
