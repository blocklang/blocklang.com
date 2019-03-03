import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import { v, w } from '@dojo/framework/widget-core/d';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import { Project, ProjectResource, CommitInfo } from '../../interfaces';
import Moment from '../../widgets/moment';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import { IconName, IconPrefix } from '@fortawesome/fontawesome-svg-core';
import MarkdownPreview from '../../widgets/markdown-preview';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';

import * as c from '../../className';
import * as css from './ViewProject.m.css';

export interface ViewProjectProperties {
	loggedUsername: string;
	project: Project;
	projectResources: ProjectResource[];
	latestCommitInfo: CommitInfo;
	readme?: string;
}

@theme(css)
export default class ViewProject extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderTable(),
			this._renderReadme()
		]);
	}

	private _renderHeader() {
		const { messages } = this._localizedMessages;
		const {
			project: { name, isPublic, createUserName }
		} = this.properties;
		return v('div', { classes: [c.mt_4, c.mb_3] }, [
			v('nav', { classes: [c.d_inline_flex], 'aria-label': 'breadcrumb' }, [
				v('ol', { classes: [css.projectHeader, c.breadcrumb] }, [
					v('li', { classes: [c.breadcrumb_item] }, [
						isPublic
							? null
							: w(FontAwesomeIcon, {
									icon: 'lock',
									size: 'xs',
									classes: [c.text_muted, c.mr_1],
									title: `${messages.privateProjectTitle}`
							  }),
						w(Link, { to: 'profile', params: { user: createUserName } }, [`${createUserName}`])
					]),
					v('li', { classes: [c.breadcrumb_item, c.active] }, [
						w(Link, { to: 'view-project', params: { owner: createUserName, project: name } }, [
							v('strong', [`${name}`])
						])
					])
				])
			])
		]);
	}

	private _renderTable() {
		return v('div', { classes: [c.card] }, [this._renderLatestCommitInfo(), this._renderResources()]);
	}

	/**
	 *  最近提交信息区
	 */
	private _renderLatestCommitInfo() {
		const { messages } = this._localizedMessages;
		const { latestCommitInfo } = this.properties;
		return v('div', { classes: [c.card_header, c.text_muted, c.px_2, c.border_bottom_0, css.recentCommit] }, [
			// 最近提交的用户信息
			w(Link, { to: 'profile', params: { user: latestCommitInfo.userName }, classes: [c.mr_2] }, [
				v('img', {
					width: 20,
					height: 20,
					classes: [c.avatar, c.mr_1],
					src: `${latestCommitInfo.avatarUrl}`
				}),
				`${latestCommitInfo.userName}`
			]),
			// 最近提交说明
			v('span', [`${latestCommitInfo.shortMessage}`]),
			// 最近提交时间
			v('span', { classes: [c.float_right] }, [
				`${messages.latestCommitLabel}`,
				w(Moment, { datetime: latestCommitInfo.commitTime })
			])
		]);
	}

	private _renderResources() {
		const { projectResources } = this.properties;
		return v('table', { classes: [c.table, c.table_hover, c.mb_0] }, [
			v('tbody', projectResources.map((resource) => this._renderTr(resource)))
		]);
	}

	private _renderTr(projectResource: ProjectResource) {
		return v('tr', [
			// 图标
			v('td', { classes: [css.icon] }, [
				w(FontAwesomeIcon, {
					icon: projectResource.icon.split(' ') as [IconPrefix, IconName],
					title: projectResource.title
				})
			]),
			// 资源名称
			v('td', { classes: [css.content, c.px_1] }, [
				v('span', { classes: [css.truncate] }, [
					w(Link, { to: '', title: `${projectResource.name}` }, [`${projectResource.name}`])
				])
			]),
			// 最近提交信息
			v('td', { classes: [css.message, c.text_muted] }, [
				v('span', { classes: [css.truncate] }, [
					v('a', { title: `${projectResource.latestFullMessage}` }, [`${projectResource.latestShortMessage}`])
				])
			]),
			// 最近提交时间
			v('td', { classes: [css.age, c.text_muted] }, [
				// 使用 moment.js 进行格式化
				v('span', { classes: [css.truncate] }, [w(Moment, { datetime: `${projectResource.latestCommitTime}` })])
			])
		]);
	}

	private _renderReadme() {
		const projectFile = { name: 'README.md' };
		const canEditProject = false;

		const { readme = '' } = this.properties;

		return v('div', { classes: [c.card, c.mt_3] }, [
			v('div', { classes: [c.card_header, c.px_2] }, [
				v('div', {}, [w(FontAwesomeIcon, { icon: 'book-open' }), ` ${projectFile.name}`]),
				canEditProject ? w(Link, { to: '' }, [w(FontAwesomeIcon, { icon: 'edit' })]) : null
			]),

			v('div', { classes: [c.card_body, c.markdown_body] }, [w(MarkdownPreview, { value: readme })])
		]);
	}
}
