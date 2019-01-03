import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';

import * as css from './styles/About.m.css';

export default class About extends WidgetBase {
	protected render() {
		return v('h1', { classes: [css.root] }, ['About Page']);
	}
}
