import { v, w } from '@dojo/framework/core/vdom';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/core/mixins/Themed';

import messageBundle from '../../nls/main';
import Link from '@dojo/framework/routing/Link';
import MarkdownPreview from '../../widgets/markdown-preview';

import 'github-markdown-css/github-markdown.css';
import 'highlight.js/styles/github.css';
import * as c from '@blocklang/bootstrap-classes';
import * as css from './ViewDocument.m.css';

export interface ViewDocumentProperties {
	content: string;
}

@theme(css)
export default class ViewDocument extends ThemedMixin(I18nMixin(WidgetBase))<ViewDocumentProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const {
			messages: { gettingStartedLabel },
		} = this._localizedMessages;
		const { content } = this.properties;

		return v('div', { classes: [css.root, c.container] }, [
			v('div', { classes: [c.row, c.mt_4] }, [
				v('div', { classes: [c.col_2, c.border_right] }, [
					v('ul', { classes: [c.nav, c.flex_column] }, [
						v('li', { classes: [c.nav_item] }, [
							w(
								Link,
								{
									to: 'docs',
									params: { fileName: 'getting-started' },
									classes: [c.nav_link, c.active],
								},
								[`${gettingStartedLabel}`]
							),
						]),
					]),
				]),
				v('div', { classes: [c.col_10] }, [
					v('div', { classes: [c.markdown_body] }, [w(MarkdownPreview, { value: content })]),
				]),
			]),
		]);
	}
}
