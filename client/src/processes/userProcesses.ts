import { createProcess } from '@dojo/framework/stores/process';
import { replace } from '@dojo/framework/stores/state/operations';
import { commandFactory } from './utils';
import { baseUrl } from '../config';
import { NicknamePayload, WebsiteUrlPayload, CompanyPayload, LocationPayload, BioPayload } from './interfaces';

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

export const getCurrentUserProcess = createProcess('get-current-user', [getCurrentUserCommand]);
export const initForUserProfileProcess = createProcess('init-for-user-profile', [getProfileCommand]);
export const nicknameInputProcess = createProcess('nickname-input', [nicknameInputCommand]);
export const websiteUrlInputProcess = createProcess('website-url-input', [websiteUrlInputCommand]);
export const companyInputProcess = createProcess('company-input', [companyInputCommand]);
export const locationInputProcess = createProcess('location-input', [locationInputCommand]);
export const bioInputProcess = createProcess('bio-input', [bioInputCommand]);
export const updateUserProfileProcess = createProcess('update-user-profile', [updateProfileCommand]);
export const closeSuccessAlertProcess = createProcess('close-success-alert', [closeSuccessAlertCommand]);
