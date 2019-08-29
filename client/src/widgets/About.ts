import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v } from '@dojo/framework/core/vdom';

import * as css from './About.m.css';

export default class About extends WidgetBase {
	protected render() {
		return v('h1', { classes: [css.root] }, ['About Page']);
	}
}
