import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';

import * as css from './styles/pagination.m.css';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import * as c from '../../className';
import { v, w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';

export interface PaginationProperties {
	totalPages: number;
	number: number;
	size: number;
	first: boolean;
	last: boolean;
}

@theme(css)
export default class Pagination extends ThemedMixin(I18nMixin(WidgetBase))<PaginationProperties> {
	protected render() {
		const { first, last, number, totalPages } = this.properties;

		// 只有当页数大于 1 时才显示分页栏
		if (totalPages <= 1) {
			return;
		}

		return v('nav', { 'aria-label': 'Page', classes: [c.my_4] }, [
			v(
				'ul',
				{
					classes: [c.pagination, c.justify_content_center]
				},
				[
					v('li', { classes: [c.page_item, first ? c.disabled : undefined] }, [
						first
							? v('a', { classes: [c.page_link], tabIndex: -1, 'aria-disabled': 'true' }, ['上一页'])
							: w(
									Link,
									{
										classes: [c.page_link],
										to: 'marketplace',
										params: { page: `${number - 1}` }
									},
									['上一页']
							  )
					]),
					v('li', { classes: [c.page_item, last ? c.disabled : undefined] }, [
						last
							? v('a', { classes: [c.page_link], tabIndex: -1, 'aria-disabled': 'true' }, ['下一页'])
							: w(
									Link,
									{
										classes: [c.page_link],
										to: 'marketplace',
										params: { page: `${number + 1}` }
									},
									['下一页']
							  )
					])
				]
			)
		]);
	}
}
