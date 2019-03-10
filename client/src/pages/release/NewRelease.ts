import { v, w } from '@dojo/framework/widget-core/d';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as c from '../../className';
import * as css from './NewRelease.m.css';
import messageBundle from '../../nls/main';
import ProjectHeader from '../widgets/ProjectHeader';
import { Project } from '../../interfaces';

export interface NewReleaseProperties {
	project: Project;
}

@theme(css)
export default class NewRelease extends ThemedMixin(I18nMixin(WidgetBase))<NewReleaseProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [css.root, c.container] }, [this._renderHeader()]);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}
}
