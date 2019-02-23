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
