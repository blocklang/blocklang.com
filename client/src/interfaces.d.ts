import { ValidateStatus } from './constant';

// 注意：一些公共信息，要做成全局变量，不然会存储很多无用的信息

export type WithTarget<T extends Event = Event, E extends HTMLElement = HTMLInputElement> = T & { target: E };

export interface ResourceBased {
	loading: boolean;
	loaded: boolean;
}

/**
 * 统一在此处存储错误信息
 */
export interface Errors {
	[index: string]: string[];
}

/**
 * 存储路由信息，用编程方式调整路由
 */
export interface Routing {
	outlet: string;
	params: { [index: string]: string };
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
	description?: string;
	isPublic: boolean;
	lastActiveTime: string;
	createUserName: string;
	createTime: string;
	createUserId: number;
	lastUpdateTime?: string;
	lastUpdateUserId?: number;
}

export interface ProjectInputValidation {
	nameValidateStatus: ValidateStatus;
	nameErrorMessage: string;
}

export interface ProjectResource {
	id: number;
	key: string;
	name: string;
	description: string;
	resourceType: string;
	parentId: number;
	seq: number;
	createTime: string;
	createUserId: number;
	lastUpdateTime: string;
	lastUpdateUserId: number;
	icon: string; // 图标样式类
}

export interface CommitInfo extends User {
	id: string;
	commitTime: string;
	shortMessage: string;
	fullMessage: string;
}

export interface State {
	errors: Errors;
	routing: Routing;
	user: User;
	projectParam: ProjectParam;
	projectInputValidation: ProjectInputValidation;
	project: Project;
	canAccessProjects: Project[];
	projectResources: ProjectResource[];
	latestCommitInfo: CommitInfo;
}
