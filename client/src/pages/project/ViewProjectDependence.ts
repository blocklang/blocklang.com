import * as css from './ViewProjectDependence.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import * as c from '../../className';
import {
	Project,
	ProjectGroup,
	CommitInfo,
	ProjectDependence,
	WithTarget,
	PagedComponentRepos,
	ComponentRepoInfo
} from '../../interfaces';
import Spinner from '../../widgets/spinner';
import { isEmpty, getProgramingLanguageName, getRepoCategoryName, getProgramingLanguageColor } from '../../util';
import Exception from '../error/Exception';
import ProjectHeader from '../widgets/ProjectHeader';
import messageBundle from '../../nls/main';

import { ProjectResourcePathPayload, QueryPayload } from '../../processes/interfaces';
import LatestCommitInfo from './widgets/LatestCommitInfo';
import ProjectResourceBreadcrumb from './widgets/ProjectResourceBreadcrumb';
import watch from '@dojo/framework/widget-core/decorators/watch';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Pagination from '../../widgets/pagination';
import Moment from '../../widgets/moment';

export interface ViewProjectDependenceProperties {
	loggedUsername: string;
	project: Project;
	sourceId: number;
	pathes: ProjectGroup[];
	pagedComponentRepos: PagedComponentRepos;
	dependences: ProjectDependence[];
	latestCommitInfo: CommitInfo;
	onOpenGroup: (opt: ProjectResourcePathPayload) => void;
	onQueryComponentRepos: (opt: QueryPayload) => void;
}

@theme(css)
export default class ViewProjectDependence extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectDependenceProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	@watch()
	private _search: string = '';

	protected render() {
		const { project } = this.properties;
		if (!project) {
			return v('div', { classes: [c.mt_5] }, [w(Spinner, {})]);
		}

		if (this._isNotFound()) {
			return w(Exception, { type: '404' });
		}

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderNavigation(),
			this._renderDependenceCard()
		]);
	}

	private _isNotFound() {
		const { project } = this.properties;
		return isEmpty(project);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle }
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderNavigation() {
		const { project, pathes, onOpenGroup } = this.properties;

		return v('div', { classes: [c.d_flex, c.justify_content_between, c.mb_2] }, [
			v('div', {}, [w(ProjectResourceBreadcrumb, { project, pathes, onOpenGroup })])
		]);
	}

	private _renderDependenceCard() {
		const { latestCommitInfo } = this.properties;

		return v('div', { classes: [c.card, !latestCommitInfo ? c.border_top_0 : undefined] }, [
			w(LatestCommitInfo, { latestCommitInfo }), // 最近提交信息区
			this._renderDependenceEditor()
		]);
	}

	private _renderDependenceEditor() {
		return v('div', { classes: [c.card_body] }, [
			this._renderComponentRepoSearchPart(),
			// 显示项目依赖
			// 1. 如果没有依赖，则显示提示信息
			// 2. 否则显示依赖
			this._renderDependencePart()
		]);
	}

	private _renderComponentRepoSearchPart() {
		return v('div', { classes: [c.py_4, c.border_bottom] }, [
			this._renderSearchForm(),
			this._renderSearchTip(),
			this._renderSearchedComponentRepos()
		]);
	}

	private _renderSearchForm() {
		const {
			messages: { componentSearchForProjectPlaceholder }
		} = this._localizedMessages;

		return v('form', {}, [
			v('div', { classes: [c.form_group] }, [
				v('input', {
					type: 'text',
					classes: [c.form_control],
					placeholder: `${componentSearchForProjectPlaceholder}`,
					oninput: this._onSearchComponentRepo,
					value: `${this._search}`
				})
			])
		]);
	}

	private _renderSearchTip() {
		if (this._search === '') {
			return;
		}

		const { pagedComponentRepos } = this.properties;

		let length = 0;
		if (pagedComponentRepos && pagedComponentRepos.content) {
			length = pagedComponentRepos.content.length;
		}

		return v('div', { classes: [c.d_flex, c.justify_content_between, c.align_items_center, c.border_bottom] }, [
			v('div', [
				'使用 ',
				v('strong', [`${this._search}`]),
				' 共查出 ',
				v('strong', [`${length}`]),
				' 个组件仓库'
			]),
			v('div', [
				v(
					'button',
					{
						classes: [c.btn, c.btn_link, c.btn_sm, css.btnLink],
						onclick: this._onClearSearchText
					},
					[w(FontAwesomeIcon, { icon: 'times', classes: [c.mr_1] }), '清空搜索条件']
				)
			])
		]);
	}

	private _onClearSearchText() {
		this._search = '';
		this.properties.onQueryComponentRepos({ query: this._search });
	}

	private _onSearchComponentRepo({ target: { value: query } }: WithTarget) {
		this._search = query;
		this.properties.onQueryComponentRepos({ query });
	}

	private _renderSearchedComponentRepos() {
		const { pagedComponentRepos } = this.properties;

		if (!pagedComponentRepos) {
			return;
		}

		if (pagedComponentRepos.content.length === 0) {
			return this._renderEmptyComponentRepo();
		}

		return v('div', { key: 'component-repos-part', classes: [] }, [
			// 组件库列表
			this._renderComponentRepos(),
			// 分页
			this._renderPagination()
		]);
	}

	private _renderEmptyComponentRepo() {
		return v(
			'div',
			{
				key: 'no-component-repos',
				classes: [c.alert, c.alert_secondary, c.mx_auto, c.text_center, c.mt_3, c.py_4]
			},
			[v('strong', {}, ['没有查到组件仓库'])]
		);
	}

	private _renderComponentRepos() {
		const { pagedComponentRepos } = this.properties;

		return v(
			'ul',
			{ classes: [c.list_group, c.mt_2] },
			pagedComponentRepos.content.map((item) => {
				// TODO: 添加判断逻辑
				const used = false;

				return w(ComponentRepoItem, { componentRepoInfo: item, used });
			})
		);
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

	private _renderDependencePart() {
		const { dependences = [] } = this.properties;
		if (dependences.length === 0) {
			return this._renderNoDependenceMessage();
		}
		return this._renderDependenceItems();
	}

	private _renderDependenceItems() {
		return v('div', { key: 'dependence-items', classes: [c.mt_4] }, []);
	}

	private _renderNoDependenceMessage() {
		return v('div', { key: 'no-dependence', classes: [c.mt_4] }, [
			v('div', { classes: [c.alert, c.alert_primary, c.mx_auto, c.text_center, c.py_4] }, [
				v('strong', {}, ['此项目尚未配置依赖'])
			])
		]);
	}
}

interface ComponentRepoItemProperties {
	componentRepoInfo: ComponentRepoInfo;
	used: boolean;
}

class ComponentRepoItem extends ThemedMixin(I18nMixin(WidgetBase))<ComponentRepoItemProperties> {
	protected render() {
		const {
			componentRepoInfo: { componentRepo, apiRepo },
			used = false
		} = this.properties;
		const displayName = componentRepo.label ? componentRepo.label : componentRepo.name;

		// TODO: 提取为一个部件
		return v('li', { classes: [c.list_group_item] }, [
			// 如果组件库未安装，则显示“使用”按钮，否则显示“已用”文本
			v('div', {}, [
				v('span', { classes: [c.font_weight_bold, c.mr_2] }, [
					v('img', {
						width: 20,
						height: 20,
						classes: [c.avatar, c.mr_1],
						src: `${componentRepo.createUserAvatarUrl}`
					}),
					`${componentRepo.createUserName} / ${displayName}`
				]),
				v('span', { classes: [c.text_muted] }, [`${componentRepo.name}`]),
				used
					? v('span', { classes: [c.float_right, c.text_info] }, ['已用'])
					: v(
							'button',
							{
								classes: [c.btn, c.btn_secondary, c.btn_sm, c.float_right],
								onclick: this._onAddDependence
							},
							['使用']
					  )
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

	private _onAddDependence() {
		const {
			componentRepoInfo: { componentRepo, apiRepo }
		} = this.properties;
		console.log('add dependence', componentRepo, apiRepo);
	}
}
