import { AccessLevel } from './constant';

function _canWrite(accessLevel: AccessLevel) {
	return accessLevel === AccessLevel.Write || accessLevel === AccessLevel.Admin;
}

function _canRead(accessLevel: AccessLevel) {
	return accessLevel === AccessLevel.Read || accessLevel === AccessLevel.Write || accessLevel === AccessLevel.Admin;
}

export function canNewPage(accessLevel: AccessLevel) {
	return _canWrite(accessLevel);
}

export function canReadPage(accessLevel: AccessLevel) {
	return _canRead(accessLevel);
}

export function canEditPage(accessLevel: AccessLevel) {
	return _canWrite(accessLevel);
}

export function canNewGroup(accessLevel: AccessLevel) {
	return _canWrite(accessLevel);
}

export function canRelease(accessLevel: AccessLevel) {
	return _canWrite(accessLevel);
}

export function canInstall(accessLevel: AccessLevel) {
	return _canRead(accessLevel);
}

export function canCommit(accessLevel: AccessLevel) {
	return _canWrite(accessLevel);
}
