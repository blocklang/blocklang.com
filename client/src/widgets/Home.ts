import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';

import * as css from './styles/Home.m.css';

export default class Home extends WidgetBase {
	protected render() {
		return v('h1', { classes: [css.root] }, ['Home Page']);
	}
}
