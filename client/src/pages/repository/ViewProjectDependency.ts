import * as css from './ViewProjectDependency.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import * as c from '@blocklang/bootstrap-classes';
import {
	Repository,
	RepositoryResourceGroup,
	CommitInfo,
	WithTarget,
	PagedComponentRepos,
	ComponentRepoInfo,
	ApiRepo,
	ApiRepoVersion,
	ComponentRepoVersion,
	ProjectDependencyData,
} from '../../interfaces';
import Spinner from '../../widgets/spinner';
import { isEmpty, getProgramingLanguageName, getRepoCategoryName, getProgramingLanguageColor } from '../../util';
import Exception from '../error/Exception';
import RepositoryHeader from '../widgets/RepositoryHeader';
import messageBundle from '../../nls/main';

import {
	RepositoryResourcePathPayload,
	QueryPayload,
	ProjectDependencyPayload,
	ProjectDependencyIdPayload,
	ProjectDependencyVersionPayload,
	ProjectDependencyWithProjectPathPayload,
} from '../../processes/interfaces';
import LatestCommitInfo from './widgets/LatestCommitInfo';
import ProjectResourceBreadcrumb from './widgets/ProjectResourceBreadcrumb';
import watch from '@dojo/framework/core/decorators/watch';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import Pagination from '../../widgets/pagination';
import Moment from '../../widgets/moment';
import { findIndex, find } from '@dojo/framework/shim/array';
import * as lodash from 'lodash';
import { DNode } from '@dojo/framework/core/interfaces';
import { IconPrefix, IconName } from '@fortawesome/fontawesome-svg-core';
import { RepoType } from '../../constant';

export interface ViewProjectDependencyProperties {
	loggedUsername: string;
	repository: Repository;
	sourceId: number;
	pathes: RepositoryResourceGroup[];
	pagedComponentRepos: PagedComponentRepos;
	dependencies: ProjectDependencyData[];
	latestCommitInfo: CommitInfo;
	onOpenGroup: (opt: RepositoryResourcePathPayload) => void;
	onQueryComponentRepos: (opt: QueryPayload) => void;
	onAddDependency: (opt: ProjectDependencyWithProjectPathPayload) => void;
	onDeleteDependency: (opt: ProjectDependencyIdPayload) => void;
	onShowDependencyVersions: (opt: ProjectDependencyPayload) => void;
	onUpdateDependencyVersion: (opt: ProjectDependencyVersionPayload) => void;
}

interface GroupedApiRepo {
	apiRepo: ApiRepo;
	apiRepoVersions: ApiRepoVersion[];
}

@theme(css)
export default class ViewProjectDependency extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectDependencyProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	@watch()
	private _search: string = '';

	protected render() {
		const { repository } = this.properties;
		if (!repository) {
			return v('div', { classes: [c.mt_5] }, [w(Spinner, {})]);
		}

		if (this._isNotFound()) {
			return w(Exception, { type: '404' });
		}

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			this._renderNavigation(),
			this._renderDependencyCard(),
		]);
	}

	private _isNotFound() {
		const { repository } = this.properties;
		return isEmpty(repository);
	}

	private _renderHeader() {
		const {
			messages: { privateRepositoryTitle },
		} = this._localizedMessages;
		const { repository } = this.properties;

		return w(RepositoryHeader, { repository, privateRepositoryTitle });
	}

	private _renderNavigation() {
		const { repository, pathes, onOpenGroup } = this.properties;

		return v('div', { classes: [c.d_flex, c.justify_content_between, c.mb_2] }, [
			v('div', {}, [w(ProjectResourceBreadcrumb, { repository, pathes, onOpenGroup })]),
		]);
	}

	private _renderDependencyCard() {
		const { latestCommitInfo } = this.properties;

		return v('div', { classes: [c.card, !latestCommitInfo ? c.border_top_0 : undefined] }, [
			w(LatestCommitInfo, { latestCommitInfo, showBottomBorder: true }), // 最近提交信息区
			this._renderDependencyEditor(),
		]);
	}

	private _renderDependencyEditor() {
		return v('div', { classes: [c.card_body] }, [
			this._renderComponentRepoSearchPart(),
			// 显示项目依赖
			// 1. 如果没有依赖，则显示提示信息
			// 2. 否则显示依赖
			this._renderDependencyPart(),
		]);
	}

	private _renderComponentRepoSearchPart() {
		return v('div', { classes: [c.py_4, c.border_bottom] }, [
			this._renderSearchForm(),
			this._renderSearchTip(),
			this._renderSearchedComponentRepos(),
		]);
	}

	private _renderSearchForm() {
		const {
			messages: { componentSearchForProjectPlaceholder },
		} = this._localizedMessages;

		return v('form', {}, [
			v('div', { classes: [c.form_group] }, [
				v('input', {
					type: 'text',
					classes: [c.form_control],
					placeholder: `${componentSearchForProjectPlaceholder}`,
					oninput: this._onSearchComponentRepo,
					value: `${this._search}`,
				}),
			]),
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
				' 个组件仓库',
			]),
			v('div', [
				v(
					'button',
					{
						classes: [c.btn, c.btn_link, c.btn_sm, css.btnLink],
						onclick: this._onClearSearchText,
					},
					[w(FontAwesomeIcon, { icon: 'times', classes: [c.mr_1] }), '清空搜索条件']
				),
			]),
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
			this._renderPagination(),
		]);
	}

	private _renderEmptyComponentRepo() {
		return v(
			'div',
			{
				key: 'no-component-repos',
				classes: [c.alert, c.alert_secondary, c.mx_auto, c.text_center, c.mt_3, c.py_4],
			},
			[v('strong', {}, ['没有查到组件仓库'])]
		);
	}

	private _renderComponentRepos() {
		const { repository, pagedComponentRepos, dependencies = [], onAddDependency } = this.properties;

		return v(
			'ul',
			{ classes: [c.list_group, c.mt_2] },
			pagedComponentRepos.content.map((item) => {
				const used =
					findIndex(dependencies, (dependency) => item.componentRepo.id === dependency.componentRepo.id) > -1;

				return w(ComponentRepoItem, {
					repository,
					componentRepoInfo: item,
					used,
					onAddDependency,
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
			size,
		});
	}

	private _renderDependencyPart() {
		const { dependencies = [] } = this.properties;
		if (dependencies.length === 0) {
			return this._renderNoDependencyMessage();
		}
		return this._renderDependencyItems();
	}

	private _renderDependencyItems() {
		return v('div', { key: 'dependency-items', classes: [c.mt_4] }, [
			...this._renderApiRepos(),
			...this._renderDevComponentRepos(),
			...this._renderBuildComponentRepos(),
		]);
	}

	private _renderApiRepos() {
		const { dependencies = [] } = this.properties;

		const groupedApiRepos: GroupedApiRepo[] = [];

		dependencies.forEach((item) => {
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
								classes: [c.ml_1],
							},
							[`${item.apiRepo.gitRepoOwner}/${item.apiRepo.gitRepoName}`]
						),
						v(
							'span',
							{ classes: [c.ml_3] },
							item.apiRepoVersions.map((version) =>
								v('span', { classes: [c.mr_1, c.badge, c.badge_secondary] }, [`${version.version}`])
							)
						),
					])
				)
			),
		];
	}

	private _renderDevComponentRepos(): DNode[] {
		const { dependencies = [] } = this.properties;

		const devDependencies = dependencies.filter((dependency) => dependency.componentRepo.repoType === RepoType.IDE);

		if (devDependencies.length === 0) {
			return [];
		}

		return [v('div', {}, [v('strong', ['开发'])]), ...this._renderComponentRepoDependencies(devDependencies)];
	}

	private _renderBuildComponentRepos(): DNode[] {
		const { dependencies = [] } = this.properties;

		const buildDependencies = dependencies.filter(
			(dependency) => dependency.componentRepo.repoType === RepoType.PROD
		);

		if (buildDependencies.length === 0) {
			return [];
		}

		return [v('div', {}, [v('strong', ['构建'])]), ...this._renderComponentRepoDependencies(buildDependencies)];
	}

	private _renderComponentRepoDependencies(dependencies: ProjectDependencyData[]): DNode[] {
		const { repository, onDeleteDependency, onShowDependencyVersions, onUpdateDependencyVersion } = this.properties;

		// 按照 appType 分组
		const groupedDependencies = lodash.groupBy(
			dependencies,
			(dependency) => dependency.componentRepoVersion.appType
		);
		const vnodes: DNode[] = [];
		for (const key in groupedDependencies) {
			const values = groupedDependencies[key];
			vnodes.push(
				v('div', { classes: [c.pl_4, c.border_left] }, [
					v('div', {}, [`${key}`]),
					v(
						'div',
						{ classes: [c.pl_4, c.border_left] },
						values.map((item) =>
							w(DependencyRow, {
								repository,
								dependency: item,
								versions: item.componentRepoVersions || [],
								onDeleteDependency,
								onShowDependencyVersions,
								onUpdateDependencyVersion,
							})
						)
					),
				])
			);
		}
		return vnodes;
	}

	private _renderNoDependencyMessage() {
		return v('div', { key: 'no-dependency', classes: [c.mt_4] }, [
			v('div', { classes: [c.alert, c.alert_primary, c.mx_auto, c.text_center, c.py_4] }, [
				v('strong', {}, ['此项目尚未配置依赖']),
			]),
		]);
	}
}

interface ComponentRepoItemProperties {
	repository: Repository;
	componentRepoInfo: ComponentRepoInfo;
	used: boolean;
	onAddDependency: (opt: ProjectDependencyWithProjectPathPayload) => void;
}

class ComponentRepoItem extends ThemedMixin(I18nMixin(WidgetBase))<ComponentRepoItemProperties> {
	protected render() {
		const {
			componentRepoInfo: { componentRepo, componentRepoVersion, apiRepo },
			used = false,
		} = this.properties;

		return v('li', { classes: [c.list_group_item] }, [
			// 如果组件库未安装，则显示“使用”按钮，否则显示“已用”文本
			v('div', {}, [
				v('span', { classes: [c.font_weight_bold, c.mr_2] }, [
					v('img', {
						width: 20,
						height: 20,
						classes: [c.avatar, c.mr_1],
						src: `${componentRepo.createUserAvatarUrl}`,
					}),
					`${componentRepo.createUserName} / ${componentRepoVersion.name}`,
				]),
				v('span', { classes: [c.badge, c.badge_info, c.ml_3], title: '与 BlockLang 设计器集成' }, [
					`${componentRepo.repoType}`,
				]),
				used
					? v('span', { classes: [c.float_right, c.text_info] }, ['已用'])
					: v(
							'button',
							{
								classes: [c.btn, c.btn_secondary, c.btn_sm, c.float_right],
								onclick: this._onAddDependency,
							},
							['使用']
					  ),
			]),
			v('p', { itemprop: 'description', classes: [c.text_muted, c.mb_0] }, [
				`${componentRepoVersion.description}`,
			]),
			v('div', { classes: [c.my_2] }, [
				v('span', { classes: [c.border, c.rounded, c.px_1] }, [
					v('span', {}, ['API: ']),
					v(
						'a',
						{
							target: '_blank',
							href: `${apiRepo.gitRepoUrl}`,
							title: '跳转到 API 仓库',
							classes: [c.mr_1],
						},
						[`${apiRepo.gitRepoOwner}/${apiRepo.gitRepoName}`]
					),
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
							classes: [c.mr_1],
						},
						[`${componentRepo.gitRepoOwner}/${componentRepo.gitRepoName}`]
					),
				]),
			]),
			v('small', { classes: [c.text_muted] }, [
				v('span', { classes: [c.mr_3] }, [
					w(FontAwesomeIcon, {
						icon: componentRepoVersion.icon.split(' ') as [IconPrefix, IconName],
						classes: [c.mr_1],
					}),
					`${componentRepoVersion.title}`,
				]),
				v('span', { classes: [c.mr_3] }, [
					v('span', {
						classes: [css.repoLanguageColor, c.mr_1],
						styles: {
							backgroundColor: `${getProgramingLanguageColor(componentRepoVersion.language)}`,
						},
					}),
					v('span', { itemprop: 'programmingLanguage' }, [
						`${getProgramingLanguageName(componentRepoVersion.language)}`,
					]),
				]),
				v('span', { classes: [c.mr_3] }, [`${getRepoCategoryName(componentRepo.category)}`]),
				v('span', { classes: [c.mr_3], title: '使用次数' }, [
					w(FontAwesomeIcon, { icon: 'cube', classes: [c.mr_1] }),
					'0',
				]),
				v('span', {}, [
					w(FontAwesomeIcon, { icon: 'clock', classes: [c.mr_1] }),
					'最近发布 · ',
					w(Moment, { datetime: componentRepo.lastPublishTime }),
				]),
			]),
		]);
	}

	private _onAddDependency() {
		const {
			repository,
			componentRepoInfo: { componentRepo },
		} = this.properties;
		// componentRepoVersionId 默认使用最新版本
		this.properties.onAddDependency({
			owner: repository.createUserName,
			repo: repository.name,
			componentRepoId: componentRepo.id!,
			project: '', // TODO
		});
	}
}

interface DependencyRowProperties {
	repository: Repository;
	dependency: ProjectDependencyData;
	// 当前选中依赖的版本列表
	versions: ComponentRepoVersion[];
	onDeleteDependency: (opt: ProjectDependencyIdPayload) => void;
	onShowDependencyVersions: (opt: ProjectDependencyPayload) => void;
	onUpdateDependencyVersion: (opt: ProjectDependencyVersionPayload) => void;
}

class DependencyRow extends ThemedMixin(I18nMixin(WidgetBase))<DependencyRowProperties> {
	protected render() {
		const { repository, dependency, versions, onUpdateDependencyVersion } = this.properties;
		return v('div', {}, [
			// 当前只支持 git
			w(FontAwesomeIcon, { icon: ['fab', 'git-alt'], classes: [c.text_muted], title: 'git 仓库' }),
			v(
				'a',
				{
					target: '_blank',
					href: `${dependency.apiRepo.gitRepoUrl}`,
					title: '跳转到组件仓库',
					classes: [c.ml_1],
				},
				[`${dependency.componentRepo.gitRepoOwner}/${dependency.componentRepo.gitRepoName}`]
			),
			v('span', { classes: [c.ml_3] }, [
				v('span', { classes: [c.dropdown] }, [
					v(
						'button',
						{
							classes: [c.btn, c.btn_secondary, c.btn_sm, c.dropdown_toggle, css.dropdownButton],
							type: 'button',
							'data-toggle': 'dropdown',
							onclick: this._onShowVersions,
						},
						[`${dependency.componentRepoVersion.version}`]
					),
					v(
						'div',
						{ classes: [c.dropdown_menu, css.dropdownMenu] },
						versions.map((version) =>
							w(DependencyVersionMenu, { repository, dependency, version, onUpdateDependencyVersion })
						)
					),
				]),
			]),
			v('button', { type: 'button', classes: [c.close, c.float_right], onclick: this._onDeleteDependency }, [
				v('span', { 'aria-hidden': 'true', innerHTML: '&times;' }),
			]),
		]);
	}

	private _onShowVersions() {
		const { dependency } = this.properties;
		this.properties.onShowDependencyVersions({
			dependencyId: dependency.dependency.id,
			componentRepoId: dependency.componentRepo.id!,
		});
	}

	private _onDeleteDependency() {
		const { repository, dependency } = this.properties;

		this.properties.onDeleteDependency({
			owner: repository.createUserName,
			repo: repository.name,
			id: dependency.dependency.id,
			project: '', // TODO
		});
	}
}

interface DependencyVersionMenuProperties {
	repository: Repository;
	dependency: ProjectDependencyData;
	version: ComponentRepoVersion;
	onUpdateDependencyVersion: (opt: ProjectDependencyVersionPayload) => void;
}

class DependencyVersionMenu extends ThemedMixin(I18nMixin(WidgetBase))<DependencyVersionMenuProperties> {
	protected render() {
		const { dependency, version } = this.properties;
		const isSelected = version.id === dependency.componentRepoVersion.id;

		return v(
			'a',
			{
				classes: [c.dropdown_item, isSelected ? c.active : undefined],
				href: '#',
				onclick: this._onUpdateVersion,
			},
			[`${version.version}`]
		);
	}

	private _onUpdateVersion(event: MouseEvent) {
		event.stopPropagation();
		const { repository, dependency, version } = this.properties;
		const isSelected = version.id === dependency.componentRepoVersion.id;
		if (isSelected) {
			return;
		}
		this.properties.onUpdateDependencyVersion({
			owner: repository.createUserName,
			repo: repository.name,
			dependencyId: dependency.dependency.id,
			componentRepoVersionId: version.id,
		});
	}
}
