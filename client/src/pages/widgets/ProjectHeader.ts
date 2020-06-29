import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/core/mixins/Themed';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './ProjectHeader.m.css';
import { Project } from '../../interfaces';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import Link from '@dojo/framework/routing/Link';

export interface ProjectHeaderProperties extends ThemedProperties {
	project: Project;
	privateProjectTitle: string;
}

export const ThemedBase = ThemedMixin(WidgetBase);

@theme(css)
export class ProjectHeaderBase<P extends ProjectHeaderProperties = ProjectHeaderProperties> extends ThemedBase<P> {
	protected render() {
		const {
			project: { name, isPublic, createUserName },
			privateProjectTitle,
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
									title: `${privateProjectTitle}`,
							  }),
						w(Link, { to: 'profile', params: { user: createUserName } }, [`${createUserName}`]),
					]),
					v('li', { classes: [c.breadcrumb_item, c.active] }, [
						w(Link, { to: 'view-project', params: { owner: createUserName, project: name } }, [
							v('strong', [`${name}`]),
						]),
					]),
				]),
			]),
		]);
	}
}

export default class ProjectHeader extends ProjectHeaderBase<ProjectHeaderProperties> {}
