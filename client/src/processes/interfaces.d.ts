import { OutletContext } from '@dojo/framework/routing/interfaces';
import { User } from '../interfaces';

export interface ChangeRoutePayload {
	outlet: string;
	context: OutletContext;
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

export interface VersionPayload {
	owner: string;
	project: string;
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
