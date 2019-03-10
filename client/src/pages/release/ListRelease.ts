import { v, w } from '@dojo/framework/widget-core/d';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as c from '../../className';
import * as css from './ListRelease.m.css';
import messageBundle from '../../nls/main';
import ProjectHeader from '../widgets/ProjectHeader';
import { Project, Release } from '../../interfaces';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Link from '@dojo/framework/routing/Link';

export interface ListReleaseProperties {
	project: Project;
	releases: Release[];
}

@theme(css)
export default class ListRelease extends ThemedMixin(I18nMixin(WidgetBase))<ListReleaseProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { releases = [] } = this.properties;
		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			releases.length === 0 ? this._renderEmptyReleases() : this._renderReleases(releases)
		]);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderEmptyReleases() {
		const {
			messages: { releaseText, noReleaseTitle, newReleaseText, releaseDescription }
		} = this._localizedMessages;

		return v('div', {}, [
			v('div', { classes: [c.pb_4, c.mb_4, c.border_bottom] }, [
				v('button', { classes: [c.btn, c.btn_primary] }, [`${releaseText}`])
			]),
			v('div', { classes: [c.jumbotron, c.mx_auto, c.text_center], styles: { maxWidth: '544px' } }, [
				w(FontAwesomeIcon, { icon: 'tag', size: '2x', classes: [c.text_muted] }),
				v('h3', { classes: [c.mt_3] }, [`${noReleaseTitle}`]),
				v('p', {}, [`${releaseDescription}`]),
				w(Link, { classes: [c.btn, c.btn_secondary, c.btn_sm], to: 'new-release' }, [`${newReleaseText}`])
			])
		]);
	}

	private _renderReleases(releases: Release[]) {
		return v('div');
	}
}
