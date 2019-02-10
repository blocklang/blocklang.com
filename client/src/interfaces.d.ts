export interface ResourceBased {
	loading: boolean;
	loaded: boolean;
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

export interface State {
	user: User;
}
