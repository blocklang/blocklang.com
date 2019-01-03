import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';

import * as css from './styles/Profile.m.css';

export interface ProfileProperties {
	username: string;
}

export default class Profile extends WidgetBase<ProfileProperties> {
	protected render() {
		const { username } = this.properties;
		return v('h1', { classes: [css.root] }, [`Welcome ${username}!`]);
	}
}
