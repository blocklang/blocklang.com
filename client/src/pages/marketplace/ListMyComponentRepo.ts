//import messageBundle from '../../nls/main';
//import * as c from '../../className';
import * as css from './ListComponentRepo.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { ListComponentRepoProperties } from './ListComponentRepo';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';

@theme(css)
export default class ListMyComponentRepo extends ThemedMixin(I18nMixin(WidgetBase))<ListComponentRepoProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', ['登录用户发布的组件', w(Link, { to: 'new-component-repo' }, ['以 git 仓库方式导入'])]);
	}
}
