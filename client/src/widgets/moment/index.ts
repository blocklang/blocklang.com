import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/widget-core/mixins/Themed';
import { customElement } from '@dojo/framework/widget-core/decorators/customElement';

import * as moment from 'moment';
import 'moment/locale/zh-cn';

import * as css from './styles/moment.m.css';

moment.locale('zh-cn');

export interface MomentProperties extends ThemedProperties {
	datetime: string;
}

export const ThemedBase = ThemedMixin(WidgetBase);

@customElement<MomentProperties>({
	tag: 'bl-moment'
})
@theme(css)
export class MomentBase<P extends MomentProperties = MomentProperties> extends ThemedBase<P> {
	protected render() {
		const { datetime } = this.properties;
		const momentInstance = moment(datetime);
		const title = momentInstance.format();
		const fromNow = momentInstance.fromNow();

		return v('time', { datetime, title }, [fromNow]);
	}
}

export default class Moment extends MomentBase<MomentProperties> {}
