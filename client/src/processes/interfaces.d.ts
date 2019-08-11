import { OutletContext } from '@dojo/framework/routing/interfaces';
import { User } from '../interfaces';

export interface ChangeRoutePayload {
	outlet: string;
	context: OutletContext;
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

export interface ProjectPathPayload {
	owner: string;
	project: string;
}

export interface ProjectResourcePathPayload extends ProjectPathPayload {
	// 不要以 / 开头
	parentPath: string;
}

export interface VersionPayload extends ProjectPathPayload {
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

export interface GroupKeyPayload extends KeyPayload, ProjectPathPayload {
	parentId: number;
}

export interface GroupNamePayload extends NamePayload, ProjectPathPayload {
	parentId: number;
}

export interface PageKeyPayload extends GroupKeyPayload {
	appType: string;
}

export interface PageNamePayload extends GroupNamePayload {
	appType: string;
}

export interface StagedChangesPayload extends ProjectPathPayload {
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

export interface ProjectDependencePayload extends ProjectPathPayload {
	componentRepoId: number;
}
