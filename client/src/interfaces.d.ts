import { ValidateStatus, ResourceType, GitFileStatus, ReleaseResult, AccessLevel } from './constant';
import { IconName } from '@fortawesome/fontawesome-svg-core';

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
 * 存储路由信息
 */
export interface Routing {
	programmatic: boolean; // 通过编程方式导航
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
	accessLevel: AccessLevel;
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
	resourceType: ResourceType;
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
	gitStatus: GitFileStatus;
}

export interface ProjectGroup {
	name: string;
	path: string;
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

// 对应 release task
export interface ProjectRelease {
	id: number;
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

type WsEvent = 'console' | 'finish';

interface WsMessageHeader {
	lineNum: number;
	event: WsEvent;
	releaseResult?: string;
}

interface WsMessage {
	payload: string;
	headers: WsMessageHeader;
}

interface AppType {
	key: string;
	value: string;
	icon: string;
}

interface GroupParam {
	id: number;
	key: string;
	name: string;
	description: string;
	parentId: number;
}

interface GroupInputValidation {
	keyValidateStatus?: ValidateStatus;
	keyErrorMessage?: string;
	nameValidateStatus?: ValidateStatus;
	nameErrorMessage?: string;
}

interface PageParam extends GroupParam {
	appType: string;
}

interface PageInputValidation extends GroupInputValidation {}

interface ParentResource {
	id: number;
	path: string;
	parentGroups: ProjectGroup[];
}

interface UncommittedFile {
	fullKeyPath: string;
	gitStatus: GitFileStatus;
	icon: string;
	iconTitle: string;
	resourceName: string;
	parentNamePath: string;
}

interface CommitMessageParam {
	value: string;
}

export interface CommitMessageInputValidation {
	commitMessageValidateStatus?: ValidateStatus;
	commitMessageErrorMessage?: string;
}

/**
 * 通用的分页对象
 */
interface Page {
	totalPages: number;
	number: number;
	size: number;
	first: boolean;
	last: boolean;
}

interface ComponentRepo {
	id?: number;
	gitRepoUrl: string;
	gitRepoWebsite: string;
	gitRepoOwner: string;
	gitRepoName: string;
	name: string;
	version: string;
	label?: string;
	description?: string;
	logoPath: string;
	category: string;
	lastPublishTime: string;
}

interface PagedComponentRepos extends Page {
	content: ComponentRepo[];
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

	// project
	projectParam: ProjectParam;
	projectInputValidation: ProjectInputValidation;
	project: Project;
	canAccessProjects: Project[];

	parentResource: ParentResource;
	projectResources: ProjectResource[];
	latestCommitInfo: CommitInfo;
	readme: string;
	userDeployInfo: DeployInfo;

	unstagedChanges: UncommittedFile[];
	stagedChanges: UncommittedFile[];
	commitMessageParam: CommitMessageParam;
	commitMessageInputValidation: CommitMessageInputValidation;

	// new page
	appTypes: AppType[];
	pageParam: PageParam;
	pageInputValidation: PageInputValidation;
	// new group
	groupParam: GroupParam;
	groupInputValidation: GroupInputValidation;

	// release
	projectRelease: ProjectRelease;
	releases: ProjectRelease[];
	jdks: JdkInfo[];
	releaseInputValidation: ReleaseInputValidation;
	projectReleaseParam: ProjectReleaseParam;
	releaseCount: number;

	// marketplace
	pagedComponentRepos: PagedComponentRepos;
	marketplacePageStatusCode: number;

	help: Help;
}
