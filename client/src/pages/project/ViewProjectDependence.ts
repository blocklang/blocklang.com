import * as css from './ViewProjectDependence.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import * as c from '../../className';
import {
	Project,
	ProjectGroup,
	CommitInfo,
	WithTarget,
	PagedComponentRepos,
	ComponentRepoInfo,
	ApiRepo,
	ApiRepoVersion,
	ComponentRepoVersion,
	ProjectDependenceData
} from '../../interfaces';
import Spinner from '../../widgets/spinner';
import { isEmpty, getProgramingLanguageName, getRepoCategoryName, getProgramingLanguageColor } from '../../util';
import Exception from '../error/Exception';
import ProjectHeader from '../widgets/ProjectHeader';
import messageBundle from '../../nls/main';

import {
	ProjectResourcePathPayload,
	QueryPayload,
	ProjectDependencePayload,
	ProjectDependenceIdPayload,
	ProjectDependenceVersionPayload,
	ProjectDependenceWithProjectPathPayload
} from '../../processes/interfaces';
import LatestCommitInfo from './widgets/LatestCommitInfo';
import ProjectResourceBreadcrumb from './widgets/ProjectResourceBreadcrumb';
import watch from '@dojo/framework/core/decorators/watch';
import FontAwesomeIcon from '../../widgets/fontawesome-icon';
import Pagination from '../../widgets/pagination';
import Moment from '../../widgets/moment';
import { findIndex, find } from '@dojo/framework/shim/array';
import * as lodash from 'lodash';
import { DNode } from '@dojo/framework/core/interfaces';
import { IconPrefix, IconName } from '@fortawesome/fontawesome-svg-core';

export interface ViewProjectDependenceProperties {
	loggedUsername: string;
	project: Project;
	sourceId: number;
	pathes: ProjectGroup[];
	pagedComponentRepos: PagedComponentRepos;
	dependences: ProjectDependenceData[];
	latestCommitInfo: CommitInfo;
	onOpenGroup: (opt: ProjectResourcePathPayload) => void;
	onQueryComponentRepos: (opt: QueryPayload) => void;
	onAddDependence: (opt: ProjectDependenceWithProjectPathPayload) => void;
	onDeleteDependence: (opt: ProjectDependenceIdPayload) => void;
	onShowDependenceVersions: (opt: ProjectDependencePayload) => void;
	onUpdateDependenceVersion: (opt: ProjectDependenceVersionPayload) => void;
}

interface GroupedApiRepo {
	apiRepo: ApiRepo;
	apiRepoVersions: ApiRepoVersion[];
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
			w(LatestCommitInfo, { latestCommitInfo, showBottomBorder: true }), // 最近提交信息区
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

	private _renderSearchedComponentRepos(): DNode {
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
		const { project, pagedComponentRepos, dependences = [], onAddDependence } = this.properties;

		return v(
			'ul',
			{ classes: [c.list_group, c.mt_2] },
			pagedComponentRepos.content.map((item) => {
				const used =
					findIndex(dependences, (dependence) => item.componentRepo.id === dependence.componentRepo.id) > -1;

				return w(ComponentRepoItem, {
					project,
					componentRepoInfo: item,
					used,
					onAddDependence
				});
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
		return v('div', { key: 'dependence-items', classes: [c.mt_4] }, [
			...this._renderApiRepos(),
			...this._renderDevComponentRepos(),
			...this._renderBuildComponentRepos()
		]);
	}

	private _renderApiRepos() {
		const { dependences = [] } = this.properties;

		const groupedApiRepos: GroupedApiRepo[] = [];

		dependences.forEach((item) => {
			const findedApiRepo = find(
				groupedApiRepos,
				(groupedApiRepo) => item.apiRepo.id === groupedApiRepo.apiRepo.id
			);
			if (findedApiRepo) {
				// 如果已存在，则再查看版本是否添加
				const indexApiRepoVersion = findIndex(
					findedApiRepo.apiRepoVersions,
					(version) => version.id === item.apiRepoVersion.id
				);
				if (indexApiRepoVersion === -1) {
					findedApiRepo.apiRepoVersions.push(item.apiRepoVersion);
				}
			} else {
				// groupedApiRepos 中不存在时，追加
				groupedApiRepos.push({ apiRepo: item.apiRepo, apiRepoVersions: [item.apiRepoVersion] });
			}
		});

		return [
			v('div', {}, [v('strong', ['API'])]),
			v(
				'div',
				{ classes: [c.pl_4, c.border_left] },
				groupedApiRepos.map((item) =>
					v('div', {}, [
						// 当前只支持 git
						w(FontAwesomeIcon, { icon: ['fab', 'git-alt'], classes: [c.text_muted], title: 'git 仓库' }),
						v(
							'a',
							{
								target: '_blank',
								href: `${item.apiRepo.gitRepoUrl}`,
								title: '跳转到 API 仓库',
								classes: [c.ml_1]
							},
							[`${item.apiRepo.gitRepoOwner}/${item.apiRepo.gitRepoName}`]
						),
						item.apiRepo.label
							? v('span', { classes: [c.text_muted, c.ml_1] }, [`${item.apiRepo.label}`])
							: undefined,
						v(
							'span',
							{ classes: [c.ml_3] },
							item.apiRepoVersions.map((version) =>
								v('span', { classes: [c.mr_1, c.badge, c.badge_secondary] }, [`${version.version}`])
							)
						)
					])
				)
			)
		];
	}

	private _renderDevComponentRepos(): DNode[] {
		const { dependences = [] } = this.properties;

		const devDependences = dependences.filter((dependence) => dependence.componentRepo.isIdeExtension === true);

		if (devDependences.length === 0) {
			return [];
		}

		return [v('div', {}, [v('strong', ['开发'])]), ...this._renderComponentRepoDependences(devDependences)];
	}

	private _renderBuildComponentRepos(): DNode[] {
		const { dependences = [] } = this.properties;

		const buildDependences = dependences.filter((dependence) => dependence.componentRepo.isIdeExtension === false);

		if (buildDependences.length === 0) {
			return [];
		}

		return [v('div', {}, [v('strong', ['构建'])]), ...this._renderComponentRepoDependences(buildDependences)];
	}

	private _renderComponentRepoDependences(dependences: ProjectDependenceData[]): DNode[] {
		const { project, onDeleteDependence, onShowDependenceVersions, onUpdateDependenceVersion } = this.properties;

		// 按照 appType 分组
		const groupedDependences = lodash.groupBy(dependences, (dependence) => dependence.componentRepo.appType);
		const vnodes: DNode[] = [];
		for (const key in groupedDependences) {
			const values = groupedDependences[key];
			vnodes.push(
				v('div', { classes: [c.pl_4, c.border_left] }, [
					v('div', {}, [`${key}`]),
					v(
						'div',
						{ classes: [c.pl_4, c.border_left] },
						values.map((item) =>
							w(DependenceRow, {
								project,
								dependence: item,
								versions: item.componentRepoVersions || [],
								onDeleteDependence,
								onShowDependenceVersions,
								onUpdateDependenceVersion
							})
						)
					)
				])
			);
		}
		return vnodes;
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
	project: Project;
	componentRepoInfo: ComponentRepoInfo;
	used: boolean;
	onAddDependence: (opt: ProjectDependenceWithProjectPathPayload) => void;
}

class ComponentRepoItem extends ThemedMixin(I18nMixin(WidgetBase))<ComponentRepoItemProperties> {
	protected render() {
		const {
			componentRepoInfo: { componentRepo, apiRepo },
			used = false
		} = this.properties;

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
					`${componentRepo.createUserName} / ${componentRepo.name}`
				]),
				componentRepo.label ? v('span', { classes: [c.text_muted] }, [`${componentRepo.label}`]) : undefined,
				componentRepo.isIdeExtension
					? v('span', { classes: [c.badge, c.badge_info, c.ml_3], title: '与 BlockLang 设计器集成' }, [
							'设计器扩展'
					  ])
					: undefined,
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
					w(FontAwesomeIcon, {
						icon: componentRepo.icon.split(' ') as [IconPrefix, IconName],
						classes: [c.mr_1]
					}),
					`${componentRepo.title}`
				]),
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
			project,
			componentRepoInfo: { componentRepo }
		} = this.properties;
		// componentRepoVersionId 默认使用最新版本
		this.properties.onAddDependence({
			owner: project.createUserName,
			project: project.name,
			componentRepoId: componentRepo.id!
		});
	}
}

interface DependenceRowProperties {
	project: Project;
	dependence: ProjectDependenceData;
	// 当前选中依赖的版本列表
	versions: ComponentRepoVersion[];
	onDeleteDependence: (opt: ProjectDependenceIdPayload) => void;
	onShowDependenceVersions: (opt: ProjectDependencePayload) => void;
	onUpdateDependenceVersion: (opt: ProjectDependenceVersionPayload) => void;
}

class DependenceRow extends ThemedMixin(I18nMixin(WidgetBase))<DependenceRowProperties> {
	protected render() {
		const { project, dependence, versions, onUpdateDependenceVersion } = this.properties;
		return v('div', {}, [
			// 当前只支持 git
			w(FontAwesomeIcon, { icon: ['fab', 'git-alt'], classes: [c.text_muted], title: 'git 仓库' }),
			v(
				'a',
				{
					target: '_blank',
					href: `${dependence.apiRepo.gitRepoUrl}`,
					title: '跳转到组件仓库',
					classes: [c.ml_1]
				},
				[`${dependence.componentRepo.gitRepoOwner}/${dependence.componentRepo.gitRepoName}`]
			),
			dependence.componentRepo.label
				? v('span', { classes: [c.text_muted, c.ml_1] }, [`${dependence.componentRepo.label}`])
				: undefined,
			v('span', { classes: [c.ml_3] }, [
				v('span', { classes: [c.dropdown] }, [
					v(
						'button',
						{
							classes: [c.btn, c.btn_secondary, c.btn_sm, c.dropdown_toggle, css.dropdownButton],
							type: 'button',
							'data-toggle': 'dropdown',
							onclick: this._onShowVersions
						},
						[`${dependence.componentRepoVersion.version}`]
					),
					v(
						'div',
						{ classes: [c.dropdown_menu, css.dropdownMenu] },
						versions.map((version) =>
							w(DependenceVersionMenu, { project, dependence, version, onUpdateDependenceVersion })
						)
					)
				])
			]),
			v('button', { type: 'button', classes: [c.close, c.float_right], onclick: this._onDeleteDependence }, [
				v('span', { 'aria-hidden': 'true', innerHTML: '&times;' })
			])
		]);
	}

	private _onShowVersions() {
		const { dependence } = this.properties;
		this.properties.onShowDependenceVersions({
			dependenceId: dependence.dependence.id,
			componentRepoId: dependence.componentRepo.id!
		});
	}

	private _onDeleteDependence() {
		const { project, dependence } = this.properties;

		this.properties.onDeleteDependence({
			owner: project.createUserName,
			project: project.name,
			id: dependence.dependence.id
		});
	}
}

interface DependenceVersionMenuProperties {
	project: Project;
	dependence: ProjectDependenceData;
	version: ComponentRepoVersion;
	onUpdateDependenceVersion: (opt: ProjectDependenceVersionPayload) => void;
}

class DependenceVersionMenu extends ThemedMixin(I18nMixin(WidgetBase))<DependenceVersionMenuProperties> {
	protected render() {
		const { dependence, version } = this.properties;
		const isSelected = version.id === dependence.componentRepoVersion.id;

		return v(
			'a',
			{
				classes: [c.dropdown_item, isSelected ? c.active : undefined],
				href: '#',
				onclick: this._onUpdateVersion
			},
			[`${version.version}`]
		);
	}

	private _onUpdateVersion(event: MouseEvent) {
		event.stopPropagation();
		const { project, dependence, version } = this.properties;
		const isSelected = version.id === dependence.componentRepoVersion.id;
		if (isSelected) {
			return;
		}
		this.properties.onUpdateDependenceVersion({
			owner: project.createUserName,
			project: project.name,
			dependenceId: dependence.dependence.id,
			componentRepoVersionId: version.id
		});
	}
}
