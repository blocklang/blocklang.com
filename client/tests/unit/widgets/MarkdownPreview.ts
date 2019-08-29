const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness';
import { w, v } from '@dojo/framework/core/vdom';

import MarkdownPreview from '../../../src/widgets/markdown-preview/index';

describe('MarkdownPreview', () => {
	it('default renders correctly', () => {
		const h = harness(() => w(MarkdownPreview, { value: '# hi' }));
		h.expect(() => v('article', { classes: ['markdown-body'], innerHTML: '<h1 id="hi">hi</h1>\n' }));
	});
});
