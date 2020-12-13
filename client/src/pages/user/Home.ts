import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import { HomeProperties } from '../Home';

import I18nMixin from '@dojo/framework/core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/core/mixins/Themed';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './Home.m.css';
import Link from '@dojo/framework/routing/Link';
import messageBundle from '../../nls/main';
import { Repository } from '../../interfaces';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';

/**
 * 个人首页
 */
@theme(css)
export default class Home extends ThemedMixin(I18nMixin(WidgetBase))<HomeProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { messages } = this._localizedMessages;
		const { canAccessRepos = [] } = this.properties;
		return v('div', { classes: [c.container, c.mt_4] }, [
			v('div', { classes: [c.row] }, [
				v('div', { classes: [c.col_md_8] }, this._renderEmptyActivity()),
				v('div', { classes: [c.col_md_4] }, [
					v('div', { classes: [c.card] }, [
						v('div', { classes: [c.card_header] }, [
							`${messages.canAccessProjectsCardTitle}`,
							w(Link, { to: 'new-repo', classes: [c.float_end], title: `${messages.newRepository}` }, [
								w(FontAwesomeIcon, { icon: 'plus' }),
							]),
						]),
						v(
							'ul',
							{ classes: [c.list_group, c.list_group_flush] },
							canAccessRepos.length === 0 ? this._renderEmptyProject() : this._renderRepos(canAccessRepos)
						),
					]),
				]),
			]),
		]);
	}

	private _renderEmptyActivity() {
		const {
			messages: {
				noActivityMessageHeader,
				noActivityMessageHelpTitle,
				noActivityMessageHelp1,
				noActivityMessageHelp2,
			},
		} = this._localizedMessages;

		return [
			v('div', { classes: [css.newsfeedPlaceholder, c.px_5, c.pt_5, c.pb_3] }, [
				v('h2', { classes: [c.text_center, c.mb_5] }, [`${noActivityMessageHeader}`]),
				v('h5', [`${noActivityMessageHelpTitle}`]),
				v('ul', { classes: [c.list_unstyled] }, [
					v('li', [`${noActivityMessageHelp1}`]),
					v('li', [`${noActivityMessageHelp2}`]),
				]),
			]),
		];
	}

	private _renderEmptyProject() {
		const { messages } = this._localizedMessages;

		return [
			v('li', { classes: [c.list_group_item] }, [
				v('div', { classes: [c.text_center] }, [
					w(FontAwesomeIcon, { icon: 'info-circle', classes: [c.text_muted] }),
					` ${messages.noProjectMessage}`,
				]),
			]),
		];
	}

	private _renderRepos(repos: Repository[]) {
		const {
			messages: { privateRepositoryTitle },
		} = this._localizedMessages;

		return repos.map((repository) => {
			return v('li', { classes: [c.list_group_item] }, [
				w(Link, { to: 'view-repo', params: { owner: repository.createUserName, repo: repository.name } }, [
					`${repository.createUserName}/${repository.name}`,
				]),
				' ',
				repository.isPublic
					? null
					: w(FontAwesomeIcon, {
							icon: 'lock',
							size: 'xs',
							classes: [c.text_muted],
							title: `${privateRepositoryTitle}`,
					  }),
			]);
		});
	}
}
