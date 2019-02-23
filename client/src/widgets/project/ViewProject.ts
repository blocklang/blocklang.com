import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import * as css from '../styles/ViewProject.m.css';
import { v } from '@dojo/framework/widget-core/d';

export interface ViewProjectProperties {
	loggedUsername: string;
	name: string;
	isPublic: boolean;
}

@theme(css)
export default class ViewProject extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectProperties> {
	protected render() {
		const { loggedUsername, name } = this.properties;
		return v('div', { classes: [css.root] }, [
			v('nav', { 'aria-label': 'breadcrumb' }, [
				v('ol', { classes: ['breadcrumb'] }, [
					v('li', { classes: ['breadcrumb-item'] }, [v('a', { href: '#' }, [`${loggedUsername}`])]),
					v('li', { classes: ['breadcrumb-item'] }, [v('a', { href: '#' }, [`${name}`])])
				])
			])
		]);
	}
}
