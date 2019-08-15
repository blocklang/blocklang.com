import { v, w } from '@dojo/framework/widget-core/d';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import messageBundle from '../../nls/main';
import * as c from '../../className';
import * as css from './ListComponentRepo.m.css';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Link from '@dojo/framework/routing/Link';
import { PagedComponentRepos, ComponentRepoInfo, WithTarget } from '../../interfaces';
import Spinner from '../../widgets/spinner';
import Exception from '../error/Exception';
import Moment from '../../widgets/moment';
import { getRepoCategoryName, getProgramingLanguageName, getProgramingLanguageColor } from '../../util';
import { QueryPayload } from '../../processes/interfaces';
import Pagination from '../../widgets/pagination';

export interface ListComponentRepoProperties {
	loggedUsername: string;
	pagedComponentRepos: PagedComponentRepos;
	marketplacePageStatusCode: number;
	onQueryComponentRepos: (opts: QueryPayload) => void;
}

@theme(css)
export default class ListComponentRepo extends ThemedMixin(I18nMixin(WidgetBase))<ListComponentRepoProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		const { marketplacePageStatusCode } = this.properties;

		if (marketplacePageStatusCode === 404) {
			return w(Exception, { type: '404' });
		}

		const { messages } = this._localizedMessages;
		return v('div', { classes: [css.root, c.container] }, [
			this._renderSearchBox(),

			// 发布按钮
			v('div', { classes: [c.d_flex, c.justify_content_end] }, [
				this._isLogin()
					? w(Link, { to: 'settings-marketplace', classes: [c.btn, c.btn_primary] }, [
							`${messages.publishComponentLabel}`
					  ])
					: undefined
			]),
			// 组件库列表
			this._renderCompomentReposBlock(),
			// 分页
			this._renderPagination()
		]);
	}

	private _isLogin() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderSearchBox() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.mt_5, c.mb_3] }, [
			// 标题
			v('h1', { classes: [c.text_center, c.font_weight_light] }, [`${messages.marketplaceTitle}`]),
			v('div', { classes: [c.row, c.mt_3] }, [
				v('div', { classes: [c.mx_auto, c.col_sm_12, c.col_lg_6] }, [
					v('input', {
						type: 'text',
						classes: [c.form_control],
						'aria-describedby': 'btnSearch',
						placeholder: `${messages.componentSearchPlaceholder}`,
						oninput: this._onInputQueryString
					}),
					v('small', { classes: [c.form_text, c.text_muted] }, [
						w(FontAwesomeIcon, { icon: 'lightbulb' }),
						` ${messages.componentSearchHelp}`
					])
				])
			])
		]);
	}

	private _onInputQueryString({ target: { value: query } }: WithTarget) {
		this.properties.onQueryComponentRepos({ query });
	}

	private _renderCompomentReposBlock() {
		const { pagedComponentRepos } = this.properties;

		if (!pagedComponentRepos) {
			return w(Spinner, {});
		}

		if (pagedComponentRepos.content.length === 0) {
			return this._renderEmptyComponentRepo();
		}

		return this._renderComponentRepos();
	}

	private _renderEmptyComponentRepo() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.jumbotron, c.mx_auto, c.text_center, c.mt_3], styles: { maxWidth: '544px' } }, [
			w(FontAwesomeIcon, { icon: 'puzzle-piece', size: '2x', classes: [c.text_muted] }),
			v('h3', { classes: [c.mt_3] }, [`${messages.noComponentTitle}`]),
			v('p', [
				v('ol', { classes: [c.text_left] }, [
					v('li', [`${messages.noComponentTipLine1}`]),
					v('li', [`${messages.noComponentTipLine2}`]),
					v('li', [`${messages.noComponentTipLine3}`])
				])
			]),
			this._isLogin()
				? w(
						Link,
						{
							classes: [c.btn, c.btn_secondary, c.btn_sm],
							to: 'settings-marketplace'
						},
						[`${messages.publishComponentLabel}`]
				  )
				: undefined
		]);
	}

	private _renderComponentRepos() {
		const { pagedComponentRepos } = this.properties;

		return v(
			'ul',
			{ classes: [c.list_group, c.mt_2] },
			pagedComponentRepos.content.map((item) => this._renderComponentRepoItem(item))
		);
	}

	private _renderComponentRepoItem(item: ComponentRepoInfo) {
		const { componentRepo, apiRepo } = item;
		return v('li', { classes: [c.list_group_item] }, [
			v('div', {}, [
				v('span', { classes: [c.font_weight_bold, c.mr_2] }, [
					v('img', {
						width: 20,
						height: 20,
						classes: [c.avatar, c.mr_1],
						src: `${componentRepo.createUserAvatarUrl}`
					}),
					`${componentRepo.createUserName} / ${componentRepo.name}`
				]),
				componentRepo.label ? v('span', { classes: [c.text_muted] }, [`${componentRepo.label}`]) : undefined,
				componentRepo.isIdeExtension
					? v('span', { classes: [c.badge, c.badge_info, c.ml_3], title: '与 BlockLang 设计器集成' }, [
							'设计器扩展'
					  ])
					: undefined
			]),
			v('p', { itemprop: 'description', classes: [c.text_muted, c.mb_0] }, [`${componentRepo.description}`]),
			v('div', { classes: [c.my_2] }, [
				v('span', { classes: [c.border, c.rounded, c.px_1] }, [
					v('span', {}, ['API: ']),
					v(
						'a',
						{
							target: '_blank',
							href: `${apiRepo.gitRepoUrl}`,
							title: '跳转到 API 仓库',
							classes: [c.mr_1]
						},
						[`${apiRepo.gitRepoOwner}/${apiRepo.gitRepoName}`]
					),
					// 必须确保此版本号正是最新版组件库实现的 API 版本
					v('span', {}, [`${apiRepo.version}`])
				]),
				' -> ',
				v('span', { classes: [c.border, c.rounded, c.px_1] }, [
					v('span', {}, ['实现: ']),
					v(
						'a',
						{
							target: '_blank',
							href: `${componentRepo.gitRepoUrl}`,
							title: '跳转到组件仓库',
							classes: [c.mr_1]
						},
						[`${componentRepo.gitRepoOwner}/${componentRepo.gitRepoName}`]
					),
					// 组件库的最新版本
					v('span', {}, [`${componentRepo.version}`])
				])
			]),
			v('small', { classes: [c.text_muted] }, [
				v('span', { classes: [c.mr_3] }, [
					v('span', {
						classes: [css.repoLanguageColor, c.mr_1],
						styles: {
							backgroundColor: `${getProgramingLanguageColor(componentRepo.language)}`
						}
					}),
					v('span', { itemprop: 'programmingLanguage' }, [
						`${getProgramingLanguageName(componentRepo.language)}`
					])
				]),
				v('span', { classes: [c.mr_3] }, [`${getRepoCategoryName(componentRepo.category)}`]),
				v('span', { classes: [c.mr_3], title: '使用次数' }, [
					w(FontAwesomeIcon, { icon: 'cube', classes: [c.mr_1] }),
					'0'
				]),
				v('span', {}, [
					w(FontAwesomeIcon, { icon: 'clock', classes: [c.mr_1] }),
					'最近发布 · ',
					w(Moment, { datetime: componentRepo.lastPublishTime })
				])
			])
		]);
	}

	private _renderPagination() {
		const { pagedComponentRepos } = this.properties;

		if (!pagedComponentRepos) {
			return;
		}

		const { first, last, size, number, totalPages } = pagedComponentRepos;

		return w(Pagination, {
			totalPages,
			first,
			last,
			number,
			size
		});
	}
}
