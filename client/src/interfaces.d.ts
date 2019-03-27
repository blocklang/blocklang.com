import { ValidateStatus } from './constant';
import { StringLiteral } from 'babel-types';

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
	nickname: string;
	avatarUrl: string;
}

export interface ThirdPartyUser {
	loginName: string;
	nickname: string;
	avatarUrl: string;
	needCompleteUserInfo: boolean;
	loginNameErrorMessage: string;
}

export interface ProfileParam {
	id: number;
	avatarUrl: string;
	nickname: string;
	websiteUrl: string;
	company: string;
	location: string;
	bio: string;
	loginName: string;
}

/**
 * 用户个人资料
 */
export interface Profile extends ProfileParam {
	loginName: string;
	email: string;
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
	title: string;
	latestCommitId: string;
	latestShortMessage: string;
	latestFullMessage: string;
	latestCommitTime: string;
}

export interface CommitInfo {
	id: string;
	commitTime: string;
	shortMessage: string;
	fullMessage: string;
	userName: string;
	avatarUrl: string;
}

export interface DeployInfo {
	id: number;
	projectId: number;
	userId: number;
	url: string;
	registrationToken: string;
	installerLinuxUrl: string;
	installerWindowsUrl: string;
	deployState: string;
}

type ReleaseResult = '01' | '02' | '03' | '04' | '05';

// 对应 release task
export interface ProjectRelease {
	version: string;
	releaseResult: ReleaseResult;
	title: string;
	description: string;
	createUserName: string;
	createUserAvatarUrl: string;
	createTime: string;
	jdkName: string;
	jdkVersion: string;
}

export interface ProjectReleaseParam {
	id: number;
	version: string;
	jdkReleaseId: number;
	title: string;
	description: string;
}

// 对应 app release
export interface JdkInfo {
	id: number;
	name: string;
	version: string;
}

export interface ReleaseInputValidation {
	versionValidateStatus: ValidateStatus;
	versionErrorMessage: string;
	titleValidateStatus?: ValidateStatus;
	titleErrorMessage?: string;
}

export interface UserInputValidation {
	loginNameValidateStatus?: ValidateStatus;
	loginNameErrorMessage?: string;
}

export interface Help {
	content: string;
}

export interface State {
	errors: Errors;
	routing: Routing;

	thirdPartyUser: ThirdPartyUser;
	user: User;
	profileParam: ProfileParam;
	profile: Profile;
	profileUpdateSuccessMessage: string;
	userInputValidation: UserInputValidation;

	projectParam: ProjectParam;
	projectInputValidation: ProjectInputValidation;
	project: Project;
	canAccessProjects: Project[];
	projectResources: ProjectResource[];
	latestCommitInfo: CommitInfo;
	readme: string;
	userDeployInfo: DeployInfo;

	projectRelease: ProjectRelease;
	releases: ProjectRelease[];
	jdks: JdkInfo[];
	releaseInputValidation: ReleaseInputValidation;
	projectReleaseParam: ProjectReleaseParam;
	releaseCount: number;

	help: Help;
}
