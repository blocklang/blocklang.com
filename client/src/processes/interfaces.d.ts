import { OutletContext } from '@dojo/framework/routing/interfaces';

export interface ChangeRoutePayload {
	outlet: string;
	context: OutletContext;
}

export interface NamePayload {
	name: string;
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
