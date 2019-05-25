// import messageBundle from '../../nls/main';
import * as c from '../../className';
import * as css from './NewComponentRepo.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';

export interface NewComponentRepoProperties {
	loggedUsername: string;
}

@theme(css)
export default class NewComponentRepo extends ThemedMixin(I18nMixin(WidgetBase))<NewComponentRepoProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [c.container] }, ['我发布的组件']);
	}
}
