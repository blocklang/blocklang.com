import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import { HomeProperties } from '../Home';

import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as css from './Home.m.css';
import Link from '@dojo/framework/routing/Link';
import messageBundle from '../../nls/main';
import { Project } from '../../interfaces';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';

/**
 * 个人首页
 */
@theme(css)
export default class Home extends ThemedMixin(I18nMixin(WidgetBase))<HomeProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { messages } = this._localizedMessages;
		const { canAccessProjects = [] } = this.properties;
		return v('div', { classes: ['container mt-4'] }, [
			v('div', { classes: ['row'] }, [
				v('div', { classes: ['col-md-8'] }, this._renderEmptyActivity()),
				v('div', { classes: ['col-md-4'] }, [
					v('div', { classes: ['card'] }, [
						v('div', { classes: ['card-header'] }, [
							`${messages.canAccessProjectsCardTitle}`,
							w(Link, { to: 'new-project', classes: ['float-right'], title: `${messages.newProject}` }, [
								w(FontAwesomeIcon, { icon: 'plus' })
							])
						]),
						v(
							'ul',
							{ classes: ['list-group list-group-flush'] },
							canAccessProjects.length === 0
								? this._renderEmptyProject()
								: this._renderProjects(canAccessProjects)
						)
					])
				])
			])
		]);
	}

	private _renderEmptyActivity() {
		const {
			messages: {
				noActivityMessageHeader,
				noActivityMessageHelpTitle,
				noActivityMessageHelp1,
				noActivityMessageHelp2
			}
		} = this._localizedMessages;

		return [
			v('div', { classes: [css.newsfeedPlaceholder, 'px-5 pt-5 pb-3'] }, [
				v('h2', { classes: ['text-center mb-5'] }, [`${noActivityMessageHeader}`]),
				v('h5', [`${noActivityMessageHelpTitle}`]),
				v('ul', { classes: ['list-unstyled'] }, [
					v('li', [`${noActivityMessageHelp1}`]),
					v('li', [`${noActivityMessageHelp2}`])
				])
			])
		];
	}

	private _renderEmptyProject() {
		const { messages } = this._localizedMessages;

		return [
			v('li', { classes: ['list-group-item'] }, [
				v('div', { classes: ['text-center'] }, [
					w(FontAwesomeIcon, { icon: 'info-circle', className: 'text-muted' }),
					` ${messages.noProjectMessage}`
				])
			])
		];
	}

	private _renderProjects(projects: Project[]) {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;

		return projects.map((project) => {
			return v('li', { classes: ['list-group-item'] }, [
				w(Link, { to: 'view-project', params: { owner: project.createUserName, project: project.name } }, [
					`${project.createUserName}/${project.name}`
				]),
				' ',
				project.isPublic
					? null
					: w(FontAwesomeIcon, {
							icon: 'lock',
							size: 'xs',
							className: 'text-muted',
							title: `${privateProjectTitle}`
					  })
			]);
		});
	}
}
