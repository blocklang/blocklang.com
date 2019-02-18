import { ValidateStatus } from './constant';

// 注意：一些公共信息，要做成全局变量，不然会存储很多无用的信息

export type WithTarget<T extends Event = Event, E extends HTMLElement = HTMLInputElement> = T & { target: E };

export interface ResourceBased {
	loading: boolean;
	loaded: boolean;
}

export interface Errors {
	[index: string]: string[];
}

/**
 * 登录用户信息
 *
 */
export interface User {
	userId: number;
	loginName: string;
	avatarUrl: string;
}

/**
 * 项目 form 表单输入信息
 */
export interface ProjectParam {
	id: number;
	name: string;
	description: string;
	isPublic: boolean;
}

export interface Project {
	id: number;
	name: string;
	description: string;
	isPublic: boolean;
	lastActiveTime: string;
	createUserName: string;
	createTime: string;
	createUserId: number;
	lastUpdateTime: string;
	lastUpdateUserId: number;
}

export interface ProjectInputValidation {
	nameValidateStatus: ValidateStatus;
	nameErrorMessage: string;
}

export interface State {
	errors: Errors;
	user: User;
	projectParam: ProjectParam;
	projectInputValidation: ProjectInputValidation;
	project: Project;
}
