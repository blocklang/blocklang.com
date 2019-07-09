import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

//import * as c from '../../../className';
import * as css from './ViewComponentRepoPublishTask.m.css';
import { v } from '@dojo/framework/widget-core/d';

export interface ViewComponentRepoPublishTaskProperties {}

@theme(css)
export default class ViewComponentRepoPublishTask extends ThemedMixin(I18nMixin(WidgetBase))<
	ViewComponentRepoPublishTaskProperties
> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', {}, ['view publish task...']);
	}
}
