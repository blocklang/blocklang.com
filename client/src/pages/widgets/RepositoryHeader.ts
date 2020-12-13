import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/core/mixins/Themed';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './RepositoryHeader.m.css';
import { Repository } from '../../interfaces';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import Link from '@dojo/framework/routing/Link';

export interface RepositoryHeaderProperties extends ThemedProperties {
	repository: Repository;
	privateRepositoryTitle: string;
}

export const ThemedBase = ThemedMixin(WidgetBase);

@theme(css)
export class RepositoryHeaderBase<
	P extends RepositoryHeaderProperties = RepositoryHeaderProperties
> extends ThemedBase<P> {
	protected render() {
		const {
			repository: { name, isPublic, createUserName },
			privateRepositoryTitle,
		} = this.properties;
		return v('div', { classes: [c.mt_4, c.mb_3] }, [
			v('nav', { classes: [c.d_inline_flex], 'aria-label': 'breadcrumb' }, [
				v('ol', { classes: [css.repositoryHeader, c.breadcrumb] }, [
					v('li', { classes: [c.breadcrumb_item] }, [
						isPublic
							? null
							: w(FontAwesomeIcon, {
									icon: 'lock',
									size: 'xs',
									classes: [c.text_muted, c.me_1],
									title: `${privateRepositoryTitle}`,
							  }),
						w(Link, { to: 'profile', params: { user: createUserName } }, [`${createUserName}`]),
					]),
					v('li', { classes: [c.breadcrumb_item, c.active] }, [
						w(Link, { to: 'view-repo', params: { owner: createUserName, repo: name } }, [
							v('strong', [`${name}`]),
						]),
					]),
				]),
			]),
		]);
	}
}

export default class RepositoryHeader extends RepositoryHeaderBase<RepositoryHeaderProperties> {}
