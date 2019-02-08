import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';

import * as css from './styles/Home.m.css';

export default class Home extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root, "jumbotron", "text-center"] }, [
			v("h1", { classes: [] }, ["软件拼装工厂"]),
			v('a', { classes: ["btn btn-outline-primary btn-lg my-5"], href: '/oauth2/authorization/github' }, [v('i', { classes: ['fab fa-github fa-lg'] }), ' Github 登录'])
		]);
	}
}
