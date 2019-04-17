import { v, w } from '@dojo/framework/widget-core/d';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';
import messageBundle from '../../nls/main';
import ProjectHeader from '../widgets/ProjectHeader';
import { Project, ProjectRelease } from '../../interfaces';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Link from '@dojo/framework/routing/Link';
import Spinner from '../../widgets/spinner';
import { IconName } from '@fortawesome/fontawesome-svg-core';
import MarkdownPreview from '../../widgets/markdown-preview';
import Moment from '../../widgets/moment';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';
import * as c from '../../className';
import * as css from './ListRelease.m.css';

export interface ListReleaseProperties {
	loggedUsername: string;
	project: Project;
	releases: ProjectRelease[];
}

@theme(css)
export default class ListRelease extends ThemedMixin(I18nMixin(WidgetBase))<ListReleaseProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [css.root, c.container] }, [this._renderHeader(), this._renderReleasePart()]);
	}

	private _isAuthenticated() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderReleasePart() {
		const { releases } = this.properties;
		if (releases === undefined) {
			return w(Spinner, {});
		}
		if (releases.length === 0) {
			return this._renderEmptyReleases();
		}
		return this._renderReleases(releases);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderEmptyReleases() {
		const {
			messages: { releaseText, noReleaseTitle, newReleaseText, noReleaseTip }
		} = this._localizedMessages;
		const {
			project: { createUserName, name }
		} = this.properties;

		return v('div', { classes: [c.container], styles: { maxWidth: '700px' } }, [
			v('div', { classes: [c.pb_4, c.mb_4, c.border_bottom] }, [
				w(
					Link,
					{
						classes: [c.btn, c.btn_info],
						to: 'list-release',
						params: { owner: createUserName, project: name }
					},
					[`${releaseText}`]
				)
			]),
			v('div', { classes: [c.jumbotron, c.mx_auto, c.text_center], styles: { maxWidth: '544px' } }, [
				w(FontAwesomeIcon, { icon: 'tag', size: '2x', classes: [c.text_muted] }),
				v('h3', { classes: [c.mt_3] }, [`${noReleaseTitle}`]),
				v('p', {}, [`${noReleaseTip}`]),
				this._isAuthenticated()
					? w(
							Link,
							{
								classes: [c.btn, c.btn_secondary, c.btn_sm],
								to: 'new-release',
								params: { owner: createUserName, project: name }
							},
							[`${newReleaseText}`]
					  )
					: undefined
			])
		]);
	}

	private _renderReleases(releases: ProjectRelease[]) {
		const {
			messages: { releaseText, newReleaseText }
		} = this._localizedMessages;
		const {
			project: { createUserName, name }
		} = this.properties;

		return v('div', [
			v('div', { classes: [c.pb_4, c.d_flex, c.justify_content_between] }, [
				v('div', {}, [
					w(
						Link,
						{
							classes: [c.btn, c.btn_info],
							to: 'list-release',
							params: { owner: createUserName, project: name }
						},
						[`${releaseText}`]
					)
				]),
				v('div', { classes: [] }, [
					w(
						Link,
						{
							classes: [c.btn, c.btn_outline_secondary],
							to: 'new-release',
							params: { owner: createUserName, project: name }
						},
						[`${newReleaseText}`]
					)
				])
			]),
			v(
				'div',
				{ classes: [c.border_top] },
				releases.map((release) => {
					return this._renderItem(release);
				})
			)
		]);
	}

	private _renderItem(release: ProjectRelease) {
		const { project } = this.properties;

		const releaseResult = release.releaseResult;
		let resultClasses = '';
		let spin = false;
		let resultText = '';
		let icon: IconName = 'clock';
		if (releaseResult === '01') {
			resultClasses = c.text_muted;
			resultText = '准备';
			icon = 'clock';
		} else if (releaseResult === '02') {
			spin = true;
			resultClasses = c.text_warning;
			resultText = '发布中';
			icon = 'spinner';
		} else if (releaseResult === '03') {
			resultClasses = c.text_danger;
			resultText = '失败';
			icon = 'times';
		} else if (releaseResult === '04') {
			resultClasses = c.text_success;
			resultText = '成功';
			icon = 'check';
		} else if (releaseResult === '05') {
			resultClasses = c.text_muted;
			resultText = '取消';
			icon = 'ban';
		}

		return v('div', { classes: [c.row] }, [
			v('div', { classes: [c.col_3, c.text_right, c.py_4] }, [
				v('ul', { classes: [c.list_unstyled, c.mt_2] }, [
					v('li', { classes: [c.mb_1] }, [
						w(FontAwesomeIcon, { icon: 'tag', classes: [c.text_muted] }),
						` ${release.version}`
					]),
					v('li', { classes: [c.mb_1, resultClasses] }, [
						w(FontAwesomeIcon, { icon, spin }),
						` ${resultText}`
					])
				])
			]),
			v('div', { classes: [c.col_9, css.releaseMainSection, c.py_4] }, [
				// header
				v('h2', [
					w(
						Link,
						{
							to: 'view-release',
							params: { owner: project.createUserName, project: project.name, version: release.version },
							classes: [resultClasses]
						},
						[`${release.title}`]
					)
				]),
				v('div', { classes: [c.mb_4] }, [
					v('small', { classes: [c.text_muted] }, [
						w(Link, { to: `${release.createUserName}` }, [
							v(
								'img',
								{ classes: [c.avatar], src: `${release.createUserAvatarUrl}`, width: 20, height: 20 },
								[]
							),
							' ',
							v('strong', [`${release.createUserName}`])
						]),
						' 发布于 ',
						w(Moment, { datetime: release.createTime })
					])
				]),
				// 介绍
				release.description
					? v('div', { classes: [c.markdown_body] }, [w(MarkdownPreview, { value: release.description })])
					: null,
				// jdk
				v('hr'),
				v('div', { classes: [c.text_muted, c.my_4] }, [
					w(FontAwesomeIcon, { icon: ['fab', 'java'], size: '2x' }),
					` ${release.jdkName}_${release.jdkVersion} `
				])
			])
		]);
	}
}
