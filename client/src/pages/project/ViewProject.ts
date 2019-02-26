import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import * as css from './ViewProject.m.css';
import { v, w } from '@dojo/framework/widget-core/d';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import { Project, ProjectResource } from '../../interfaces';

export interface ViewProjectProperties {
	loggedUsername: string;
	project: Project;
	projectResources: ProjectResource[];
}

@theme(css)
export default class ViewProject extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [css.root, 'container mt-4'] }, [
			this._renderHeader(),
			this._renderTable(),
			this._renderReadme()
		]);
	}

	private _renderHeader() {
		const { messages } = this._localizedMessages;
		const {
			loggedUsername,
			project: { name, isPublic, createUserName }
		} = this.properties;
		return v('nav', { classes: ['d-inline-flex'], 'aria-label': 'breadcrumb' }, [
			v('ol', { classes: [css.projectHeader, 'breadcrumb'] }, [
				v('li', { classes: ['breadcrumb-item'] }, [
					isPublic
						? null
						: v('i', {
								classes: ['fas fa-lock text-muted fa-xs mr-1'],
								title: `${messages.privateProjectTitle}`
						  }),
					w(Link, { to: 'profile', params: { user: loggedUsername } }, [`${loggedUsername}`])
				]),
				v('li', { classes: ['breadcrumb-item active'] }, [
					w(Link, { to: 'view-project', params: { owner: createUserName, project: name } }, [
						v('strong', [`${name}`])
					])
				])
			])
		]);
	}

	private _renderTable() {
		const { projectResources } = this.properties;
		// const lastCommitUser:any = {loginName: 'jack'};
		// const lastCommitMessage = "last commit messae";
		return v('div', { classes: ['card'] }, [
			// 最近提交信息区
			// 下一版本支持
			// v('div', {classes: ['card-header']}, [
			// 	// 最近更新项目的用户信息
			// 	w(Link, {to: 'profile', params: {user: lastCommitUser.loginName}, classes: ['mr-1']}, [
			// 		v('img', {width: 20, height: 20, classes: ['avatar'], src: `${lastCommitUser.avatarUrl}`}),
			// 		`${lastCommitUser.loginName}`
			// 	]),
			// 	// 最近更新说明，这里取最近 commit 信息？
			// 	// 如果取 commit 信息的话，与最近修改人就可能不是同一个人
			// 	// 但是如果是最近修改的话，并没有最近修改信息可以显示。
			// 	v('span', [`${lastCommitMessage}`])
			// ])

			v('table', { classes: ['table', 'table-hover'] }, [v('tbody', this._renderTr(projectResources))])
		]);
	}

	private _renderTr(projectResources: ProjectResource[]) {
		return projectResources.map((resource) => {
			return v('tr', [
				// 图标
				v('td', {}, [v('i', { classes: [resource.icon], title: resource.resourceType })]),
				// 资源名称
				v('td', {}, [w(Link, { to: '' }, [`${resource.name}`])]),
				// 最近提交信息
				v('td', []),
				// 最近提交时间
				v('td', {}, [
					// 使用 moment.js 进行格式化
					v('time', { title: '' }, [])
				])
			]);
		});
	}

	private _renderReadme() {
		const projectFile = { name: 'README.md' };
		const canEditProject = false;
		return v('div', { classes: ['card mt-3'] }, [
			v('div', { classes: ['card-header'] }, [
				v('div', {}, [v('i', { classes: ['fas fa-book-open'] }), ` ${projectFile.name}`]),
				canEditProject ? w(Link, { to: '' }, [v('i', { classes: 'fas fa-edit' })]) : null
			]),
			v('div', { classes: ['card-body markdown-body'] }, [
				// 这里放解析后的html，使用 innerHTM 赋值
			])
		]);
	}
}
