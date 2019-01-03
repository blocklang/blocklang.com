import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/ActiveLink';
import Toolbar from '@dojo/widgets/toolbar';

import * as css from './styles/Menu.m.css';

export default class Menu extends WidgetBase {
	protected render() {
		return w(Toolbar, { heading: 'My Dojo App!', collapseWidth: 600 }, [
			w(
				Link,
				{
					to: 'home',
					classes: [css.link],
					activeClasses: [css.selected]
				},
				['Home']
			),
			w(
				Link,
				{
					to: 'about',
					classes: [css.link],
					activeClasses: [css.selected]
				},
				['About']
			),
			w(
				Link,
				{
					to: 'profile',
					classes: [css.link],
					activeClasses: [css.selected]
				},
				['Profile']
			)
		]);
	}
}
