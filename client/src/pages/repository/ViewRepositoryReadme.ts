import * as css from './ViewRepository.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v } from '@dojo/framework/core/vdom';
import * as c from '@blocklang/bootstrap-classes';

export interface ViewRepositoryReadmeProperties {}

@theme(css)
export default class ViewRepositoryReadme extends ThemedMixin(I18nMixin(WidgetBase))<ViewRepositoryReadmeProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [c.text_center, c.mt_5] }, [v('h1', ['仓库 - README']), v('p', ['未开发'])]);
	}
}
