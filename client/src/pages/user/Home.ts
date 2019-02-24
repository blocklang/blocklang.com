import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';
import { HomeProperties } from '../Home';

/**
 * 个人首页
 */
export default class Home extends WidgetBase<HomeProperties> {
	protected render() {
		const { loggedUsername } = this.properties;
		return v('div', {}, [`${loggedUsername}`]);
	}
}
