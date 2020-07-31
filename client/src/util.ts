import * as object from '@dojo/framework/shim/object';
import { ProgrammingLanguage, RepoCategory, RepoType } from './constant';

/**
 * 确认 json 对象是否为空，即等于 `{}`
 *
 * @param obj json 对象
 */
export function isEmpty(obj: any): boolean {
	return object.entries(obj).length === 0 && obj.constructor === Object;
}

export function getProgramingLanguageColor(language: ProgrammingLanguage) {
	if (language === ProgrammingLanguage.Java) {
		return '#b07219';
	}
	if (language === ProgrammingLanguage.TypeScript) {
		return '#2b7489';
	}
	return 'white';
}

export function getProgramingLanguageName(language: ProgrammingLanguage) {
	if (language === ProgrammingLanguage.Java) {
		return 'Java';
	}
	if (language === ProgrammingLanguage.TypeScript) {
		return 'TypeScript';
	}
	return '';
}

export function getRepoCategoryName(category: RepoCategory) {
	if (category === RepoCategory.Widget) {
		return 'UI 部件';
	}
	return '';
}

export function getRepoTypeName(repoType: RepoType) {
	if (repoType === RepoType.API) {
		return 'API';
	}
	if (repoType === RepoType.PROD) {
		return 'PROD';
	}
	if (repoType === RepoType.IDE) {
		return 'IDE';
	}
}
