import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v } from '@dojo/framework/core/vdom';
import { ThemedMixin, theme, ThemedProperties } from '@dojo/framework/core/mixins/Themed';
import { customElement } from '@dojo/framework/core/decorators/customElement';

import * as marked from 'marked';
import * as hljs from 'highlight.js';
import * as DOMPurify from 'dompurify';

import * as css from './styles/markdown-preview.m.css';

export interface MarkdownPreviewProperties extends ThemedProperties {
	value: string;
}

export const ThemedBase = ThemedMixin(WidgetBase);

@customElement<MarkdownPreviewProperties>({
	tag: 'bl-markdown-preview',
})
@theme(css)
export class MarkdownPreviewBase<P extends MarkdownPreviewProperties = MarkdownPreviewProperties> extends ThemedBase<
	P
> {
	private _options: any;

	constructor() {
		super();

		this._options = {
			gfm: true,
			tables: true,
			breaks: false,
			pedantic: false,
			smartLists: true,
			smartypants: false,
			highlight: function (code: string, lang: string) {
				if (!!(lang && hljs.getLanguage(lang))) {
					return hljs.highlightAuto(code).value;
				}
				return code;
			},
		};

		marked.setOptions(this._options);
	}

	protected render() {
		const { value = '' } = this.properties;
		const innerHTML = DOMPurify.sanitize(marked(value));
		return v('article', { innerHTML });
	}
}

export default class MarkdownPreview extends MarkdownPreviewBase<MarkdownPreviewProperties> {}
