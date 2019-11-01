import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v } from '@dojo/framework/core/vdom';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/core/mixins/Themed';
import { customElement } from '@dojo/framework/core/decorators/customElement';

import * as c from '../../className';
import * as css from './styles/spinner.m.css';

type SpinnerType = 'border' | 'grow';
type Size = 'normal' | 'small';
type Color = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';

const spinnerTypeMap: { [key: string]: string } = {
	border: c.spinner_border,
	grow: c.spinner_grow
};

const textColorMap: { [key: string]: string } = {
	primary: c.text_primary,
	secondary: c.text_secondary,
	success: c.text_success,
	danger: c.text_danger,
	warning: c.text_warning,
	info: c.text_info,
	light: c.text_light,
	dark: c.text_dark
};

export interface SpinnerProperties extends ThemedProperties {
	type?: SpinnerType;
	size?: Size;
	color?: Color;
}

export const ThemedBase = ThemedMixin(WidgetBase);

@customElement<SpinnerProperties>({
	tag: 'bl-spinner'
})
@theme(css)
export class SpinnerBase<P extends SpinnerProperties = SpinnerProperties> extends ThemedBase<P> {
	protected render() {
		const { type = 'border', size = 'normal', color } = this.properties;

		let classes = [spinnerTypeMap[type.toString()]];

		if (size === 'small') {
			classes.push(type === 'border' ? c.spinner_border_sm : c.spinner_grow_sm);
		}
		if (color) {
			classes.push(textColorMap[color.toString()]);
		}

		return v('div', { classes: [c.d_flex, c.justify_content_center, c.my_3] }, [
			v('div', { classes, role: 'status' }, [v('span', { classes: [c.sr_only] }, ['Loading...'])])
		]);
	}
}

export default class Spinner extends SpinnerBase<SpinnerProperties> {}
