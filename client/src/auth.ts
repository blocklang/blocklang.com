import global from '@dojo/framework/shim/global';

export function isLogged() {
	const userSession = global.sessionStorage.getItem('blocklang-session');
	if (!userSession) {
		return false;
	}

	const user = JSON.parse(userSession);
	if (!!user.loginName) {
		return false;
	}
	return true;
}
