import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v } from '@dojo/framework/core/vdom';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/core/mixins/Themed';
import { customElement } from '@dojo/framework/core/decorators/customElement';
import { startsWith, endsWith, includes } from '@dojo/framework/shim/string';

import * as moment from 'moment';
import 'moment/locale/zh-cn';

import * as css from './styles/moment.m.css';

moment.locale('zh-cn');

export interface MomentProperties extends ThemedProperties {
	datetime: string;
}

export const ThemedBase = ThemedMixin(WidgetBase);

/**
 * TODO: 使用一个 timer 循环调度，而不是一个部件创建一个 timer
 *
 */

@customElement<MomentProperties>({
	tag: 'bl-moment',
})
@theme(css)
export class MomentBase<P extends MomentProperties = MomentProperties> extends ThemedBase<P> {
	protected render() {
		const { datetime } = this.properties;
		const momentInstance = moment(datetime);
		const title = momentInstance.format();
		let fromNow = momentInstance.fromNow();
		// 以下修改只针对中文等，不适合英语
		// 如果显示的文本中有空格，则在文本前加一个空格，如“3 秒前”改为“ 3 秒前”
		if (!startsWith(fromNow, ' ') && !endsWith(fromNow, ' ') && includes(fromNow, ' ')) {
			fromNow = ' ' + fromNow;
		}
		return v('time', { datetime, title }, [fromNow]);
	}
}

export default class Moment extends MomentBase<MomentProperties> {}
