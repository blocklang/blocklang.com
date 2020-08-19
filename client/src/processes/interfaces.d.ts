import { RouteContext } from '@dojo/framework/routing/interfaces';
import { User } from '../interfaces';

export interface ChangeRoutePayload {
	outlet: string;
	context: RouteContext;
}

export interface IdPayload {
	id: number;
}

export interface KeyPayload {
	key: string;
}

export interface NamePayload {
	name: string;
}

export interface SetSessionPayload {
	session: User;
}

export interface DescriptionPayload {
	description: string;
}

export interface IsPublicPayload {
	isPublic: boolean;
}

export interface RepositoryPathPayload {
	owner: string;
	repo: string;
}

export interface RepositoryResourcePathPayload extends RepositoryPathPayload {
	// 不要以 / 开头
	parentPath: string;
}

export interface VersionPayload extends RepositoryPathPayload {
	version: string;
}

export interface TitlePayload {
	title: string;
}

export interface JdkReleaseIdPayload {
	jdkReleaseId: number;
}

export interface NicknamePayload {
	nickname: string;
}

export interface LoginNamePayload {
	loginName: string;
}

export interface WebsiteUrlPayload {
	websiteUrl: string;
}

export interface CompanyPayload {
	company: string;
}

export interface LocationPayload {
	location: string;
}

export interface BioPayload {
	bio: string;
}

export interface GroupKeyPayload extends KeyPayload, RepositoryPathPayload {
	parentId: number;
}

export interface GroupNamePayload extends NamePayload, RepositoryPathPayload {
	parentId: number;
}

export interface PageKeyPayload extends GroupKeyPayload {
	appType: string;
}

export interface PageNamePayload extends GroupNamePayload {
	appType: string;
}

export interface StagedChangesPayload extends RepositoryPathPayload {
	files: string[];
}

export interface UnstagedChangesPayload extends StagedChangesPayload {}

export interface CommitMessagePayload {
	commitMessage: string;
}

export interface UrlPayload {
	url: string;
}

export interface QueryPayload {
	query: string;
}

export interface ProjectDependenceWithProjectPathPayload extends RepositoryPathPayload {
	project: string;
	componentRepoId: number;
}

export interface ProjectDependenceIdPayload extends RepositoryPathPayload, IdPayload {
	project: string;
}

export interface ProjectDependenceVersionPayload extends RepositoryPathPayload {
	dependenceId: number;
	componentRepoVersionId: number;
}

export interface ProjectDependencePayload {
	dependenceId: number;
	componentRepoId: number;
}
