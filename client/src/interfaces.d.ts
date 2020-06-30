import {
	ValidateStatus,
	ResourceType,
	GitFileStatus,
	ReleaseResult,
	AccessLevel,
	PublishType,
	ProgrammingLanguage,
	RepoCategory,
	PageAppType,
} from './constant';
import { IconName } from '@fortawesome/fontawesome-svg-core';

// 注意：一些公共信息，要做成全局变量，不然会存储很多无用的信息

export type WithTarget<T extends Event = Event, E extends HTMLElement = HTMLInputElement> = T & { target: E };

export interface ResourceBased {
	isLoading: boolean;
	isLoaded: boolean;
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
 *
 * 因为 Profile 这个名字，留给部件使用，这里加上 info 后缀
 */
export interface ProfileInfo extends ProfileParam {
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

export interface Project extends ResourceBased {
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

export interface ProjectResource extends ResourceBased {
	id: number;
	key: string;
	name: string;
	description: string;
	resourceType: ResourceType;
	appType: PageAppType;
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
	fullPath: string; // 从根节点到当前资源的路径，使用 / 分割
}

/**
 * @interface ProjectResourceGroup
 *
 * 项目资源分组
 *
 * @property name      分组名称
 * @property path      从根分组开始的完整路径名，使用 / 分割
 */
export interface ProjectResourceGroup {
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
	releaseResult?: ReleaseResult;
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
	appType: PageAppType;
	resourceType: ResourceType;
}

interface GroupInputValidation {
	keyValidateStatus?: ValidateStatus;
	keyErrorMessage?: string;
	nameValidateStatus?: ValidateStatus;
	nameErrorMessage?: string;
}

interface PageParam extends GroupParam {}

interface PageInputValidation extends GroupInputValidation {}

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

interface ComponentRepoPublishTask {
	id?: number;
	gitUrl: string;
	seq: number;
	website: string;
	owner: string;
	repoName: string;

	startTime: string;
	endTime: string;
	publishType: PublishType;
	publishResult: ReleaseResult;
	fromVersion: String;
	toVersion: string;

	createUserId: number;
	createUserName: string;
}

interface ApiRepo {
	id?: number;
	gitRepoUrl: string;
	gitRepoWebsite: string;
	gitRepoOwner: string;
	gitRepoName: string;
	name: string;
	version: string; // 必须存在组件市场登记的最新版本号
	label?: string;
	description?: string;
	category: RepoCategory;
	lastPublishTime: string;
	createUserName: string;
	createUserAvatarUrl: string;
}

interface ComponentRepo extends ApiRepo {
	apiRepoId: number;
	logoPath: string;
	language: ProgrammingLanguage;
	isIdeExtension: boolean;
	std: boolean;
	appType: string;
	icon: string;
	title: string;
}

interface ApiRepoVersion {
	id: number;
	apiRepoId: number;
	version: string;
}

interface ComponentRepoVersion {
	id: number;
	componentRepoId: number;
	version: string;
	apiRepoVersionId: number;
}

interface ComponentRepoInfo {
	componentRepo: ComponentRepo;
	apiRepo: ApiRepo;
}

interface PagedComponentRepos extends Page {
	content: ComponentRepoInfo[];
}

interface ComponentRepoUrlInputValidation {
	componentRepoUrlValidateStatus?: ValidateStatus;
	componentRepoUrlErrorMessage?: string;
}

interface ProjectDependence {
	id: number;
	projectId: number;
	componentRepoVersionId: number;
	profileId: number;
}

interface ProjectDependenceData extends ComponentRepoInfo {
	dependence: ProjectDependence;
	// 当前依赖的版本
	componentRepoVersion: ComponentRepoVersion;
	apiRepoVersion: ApiRepoVersion;
	// 组件仓库的所有版本
	componentRepoVersions?: ComponentRepoVersion[];
	// 如果组件仓库的版本加载失败，则显示错误信息
	loadVersionsErrorMessage?: string;
}

interface ProjectDependenceResource {
	resourceId: number;
	pathes: ProjectResourceGroup[];
	dependences: ProjectDependenceData[];
}

export interface State {
	errors: Errors;
	routing: Routing;

	thirdPartyUser: ThirdPartyUser;
	user: User;
	profileParam: ProfileParam;
	profile: ProfileInfo;
	profileUpdateSuccessMessage: string;
	userInputValidation: UserInputValidation;

	// project
	projectParam: ProjectParam;
	projectInputValidation: ProjectInputValidation;
	project: Project;
	canAccessProjects: Project[];

	// resource
	projectResource: ProjectResource; // 当前选中的项目资源，可以是分组
	parentGroups: ProjectResourceGroup[]; // 分组中不包含当前选中的资源
	childResources: ProjectResource[]; // 如果当期项目资源属于分组，则就拥有子资源

	latestCommitInfo: CommitInfo;
	readme: string;
	userDeployInfo: DeployInfo;

	// project dependences
	projectDependenceResource: ProjectDependenceResource;
	searchedComponentRepos: ComponentRepoInfo[];

	unstagedChanges: UncommittedFile[];
	stagedChanges: UncommittedFile[];
	commitMessageParam: CommitMessageParam;
	commitMessageInputValidation: CommitMessageInputValidation;

	selectedDependenceVersions: ComponentRepoVersion[];

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
	pagedComponentRepoInfos: PagedComponentRepos;
	marketplacePageStatusCode: number;
	componentRepoUrlInputValidation: ComponentRepoUrlInputValidation;
	componentRepoUrl: string;
	// 用户正在发布的组件库任务
	userComponentRepoPublishingTasks: ComponentRepoPublishTask[];
	userComponentRepoInfos: ComponentRepoInfo[];
	componentRepoPublishTask: ComponentRepoPublishTask;

	help: Help;
}
