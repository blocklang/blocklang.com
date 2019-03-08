import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/widget-core/mixins/Themed';
import { customElement } from '@dojo/framework/widget-core/decorators/customElement';

import * as c from '../../className';
import * as css from './styles/spinner.m.css';

export interface SpinnerProperties extends ThemedProperties {}

export const ThemedBase = ThemedMixin(WidgetBase);

@customElement<SpinnerProperties>({
	tag: 'bl-spinner'
})
@theme(css)
export class SpinnerBase<P extends SpinnerProperties = SpinnerProperties> extends ThemedBase<P> {
	protected render() {
		return v('div', { classes: [c.d_flex, c.justify_content_center] }, [
			v('div', { classes: [c.spinner_border], role: 'status' }, [
				v('span', { classes: [c.sr_only] }, ['Loading...'])
			])
		]);
	}
}

export default class Spinner extends SpinnerBase<SpinnerProperties> {}
