import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';

import * as css from './styles/Home.m.css';

export default class Home extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root] }, [
			v('a', { href: '/login' }, [v('i', { classes: ['fab fa-github fa-lg'] }), ' Github 帐号登录'])
		]);
	}
}
