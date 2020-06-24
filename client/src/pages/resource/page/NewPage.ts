import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import WidgetBase from '@dojo/framework/core/WidgetBase';

import messageBundle from '../../../nls/main';

import * as c from '../../../className';
import * as css from './NewPage.m.css';
import { v, w } from '@dojo/framework/core/vdom';
import Exception from '../../error/Exception';
import ProjectHeader from '../../widgets/ProjectHeader';
import { Project, AppType, WithTarget, ProjectResourceGroup } from '../../../interfaces';
import FontAwesomeIcon from '@blocklang/dojo-fontawesome/FontAwesomeIcon';
import { IconPrefix, IconName } from '@fortawesome/fontawesome-svg-core';
import Link from '@dojo/framework/routing/Link';
import { DescriptionPayload, PageKeyPayload, PageNamePayload } from '../../../processes/interfaces';
import { ValidateStatus } from '../../../constant';
import { canNewPage } from '../../../permission';

export interface NewPageProperties {
	// user
	loggedUsername: string;
	project: Project;
	appTypes: AppType[];
	// attr
	parentId: number; // 所属分组标识
	parentGroups: ProjectResourceGroup[];
	appType: string;
	// validation
	keyValidateStatus?: ValidateStatus;
	keyErrorMessage?: string;
	nameValidateStatus?: ValidateStatus;
	nameErrorMessage?: string;
	// event
	onKeyInput: (opts: PageKeyPayload) => void;
	onNameInput: (opts: PageNamePayload) => void;
	onDescriptionInput: (opts: DescriptionPayload) => void;
	onSavePage: (opts: object) => void;
}

@theme(css)
export default class NewPage extends ThemedMixin(I18nMixin(WidgetBase))<NewPageProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		// TODO: 只有授权用户才能访问，还需要判断用户对项目是否有写的权限
		if (!this._isAuthenticated()) {
			return w(Exception, { type: '403' });
		}

		const {
			messages: { newPage },
		} = this._localizedMessages;

		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			v('div', { classes: [c.container], styles: { maxWidth: '700px' } }, [
				v('div', [
					v('h4', [`${newPage}`]),
					this._renderBreadcrumb(),
					// 导航栏
					v('hr'),
					v('form', { classes: [c.needs_validation], novalidate: 'novalidate' }, [
						this._renderAppTypeRadioGroup(),
						this._renderKeyInput(),
						this._renderNameInput(),
						this._renderDescriptionTextarea(),
						v('hr'),
						this._renderButtons(),
					]),
				]),
			]),
		]);
	}

	private _isAuthenticated() {
		const { project, loggedUsername } = this.properties;
		const isLogin = !!loggedUsername;
		if (!isLogin) {
			return false;
		}
		return canNewPage(project.accessLevel);
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle },
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderBreadcrumb() {
		const { project, parentGroups } = this.properties;

		return v('nav', { classes: [], 'aria-label': 'breadcrumb' }, [
			v('ol', { classes: [c.breadcrumb, css.navOl] }, [
				// 项目名
				v('li', { classes: [c.breadcrumb_item] }, [
					w(
						Link,
						{
							to: 'view-project',
							params: { owner: project.createUserName, project: project.name },
							classes: [c.font_weight_bold],
						},
						[`${project.name}`]
					),
				]),
				...parentGroups.map((item) => {
					return v('li', { classes: [c.breadcrumb_item] }, [
						w(
							Link,
							{
								to: 'view-project-group',
								params: {
									owner: project.createUserName,
									project: project.name,
									parentPath: item.path.substring(1),
								},
							},
							[`${item.name}`]
						),
					]);
				}),
			]),
		]);
	}

	private _renderKeyInput() {
		const {
			messages: { pageKeyLabel, pageKeyHelp, requiredLabel, pageKeyPlaceholder },
		} = this._localizedMessages;

		const { keyValidateStatus = ValidateStatus.UNVALIDATED, keyErrorMessage } = this.properties;

		const inputClasses = [c.form_control];
		if (keyValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'key' }, [
				`${pageKeyLabel}`,
				v('small', { classes: [c.text_muted] }, [`${requiredLabel}`]),
			]),
			v('div', { classes: [c.input_group] }, [
				v('input', {
					type: 'text',
					id: 'key',
					classes: inputClasses,
					required: true,
					focus: true,
					maxlength: 32,
					placeholder: `${pageKeyPlaceholder}`,
					oninput: this._onKeyInput,
				}),
				// 当校验未通过时显示
				keyValidateStatus === ValidateStatus.INVALID
					? v('div', { classes: [c.invalid_tooltip], innerHTML: `${keyErrorMessage}` })
					: null,
			]),
			v('small', { classes: [c.form_text, c.text_muted] }, [`${pageKeyHelp}`]),
		]);
	}

	private _renderNameInput() {
		const {
			messages: { pageNameLabel, pageNameHelp, pageNamePlaceholder },
		} = this._localizedMessages;

		const { nameValidateStatus = ValidateStatus.UNVALIDATED, nameErrorMessage } = this.properties;

		const inputClasses = [c.form_control];
		if (nameValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'name' }, [`${pageNameLabel}`]),
			v('div', { classes: [c.input_group] }, [
				v('input', {
					type: 'text',
					id: 'name',
					classes: inputClasses,
					maxlength: 32,
					placeholder: `${pageNamePlaceholder}`,
					oninput: this._onNameInput,
				}),
				// 当校验未通过时显示
				nameValidateStatus === ValidateStatus.INVALID
					? v('div', { classes: [c.invalid_tooltip], innerHTML: `${nameErrorMessage}` })
					: null,
			]),
			v('small', { classes: [c.form_text, c.text_muted] }, [`${pageNameHelp}`]),
		]);
	}

	private _renderAppTypeRadioGroup() {
		const {
			messages: { pageTypeLabel },
		} = this._localizedMessages;

		const { appTypes } = this.properties;

		return v('div', { classes: [c.form_group] }, [
			v('label', [`${pageTypeLabel}`]),
			v('div', { classes: [c.input_group] }, [
				...appTypes.map((item, index) => {
					return v('div', { classes: [c.form_check, c.form_check_inline] }, [
						v('input', {
							classes: [c.form_check_input],
							name: 'appType',
							type: 'radio',
							id: `appType${index}`,
							checked: index === 0 ? true : false,
							value: item.key,
						}),
						v('label', { classes: [c.form_check_label], for: `appType${index}` }, [
							w(FontAwesomeIcon, { icon: item.icon.split(' ') as [IconPrefix, IconName] }),
							` ${item.value}`,
						]),
					]);
				}),
			]),
		]);
	}

	private _renderDescriptionTextarea() {
		const {
			messages: { pageDescriptionLabel },
		} = this._localizedMessages;

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'description' }, [`${pageDescriptionLabel}`]),
			v('textarea', {
				classes: [c.form_control],
				rows: 2,
				id: 'description',
				maxlength: 64,
				oninput: this._onDescriptionInput,
			}),
		]);
	}

	private _renderButtons() {
		const {
			messages: { pageSaveLabel, pageCancelSaveLabel },
		} = this._localizedMessages;

		const {
			project,
			parentGroups = [],
			keyValidateStatus = ValidateStatus.UNVALIDATED,
			nameValidateStatus = ValidateStatus.UNVALIDATED,
		} = this.properties;
		// name 默认可以为空，所以可以不用填写，即不走校验。
		const disabled =
			keyValidateStatus === ValidateStatus.VALID && nameValidateStatus !== ValidateStatus.INVALID ? false : true;

		return v('div', [
			v(
				'button',
				{
					type: 'button',
					classes: [c.btn, c.btn_primary],
					disabled,
					onclick: disabled ? undefined : this._onSavePage,
				},
				[`${pageSaveLabel}`]
			),
			' ',
			parentGroups.length === 0
				? w(
						Link,
						{
							classes: [c.btn, c.btn_secondary],
							to: 'view-project',
							params: { owner: project.createUserName, project: project.name },
						},
						[`${pageCancelSaveLabel}`]
				  )
				: w(
						Link,
						{
							classes: [c.btn, c.btn_secondary],
							to: 'view-project-group',
							params: {
								owner: project.createUserName,
								project: project.name,
								parentPath: parentGroups[parentGroups.length - 1].path.substring(1),
							},
						},
						[`${pageCancelSaveLabel}`]
				  ),
		]);
	}

	private _onKeyInput({ target: { value: key } }: WithTarget) {
		const {
			project: { createUserName, name },
			parentId,
			appType,
		} = this.properties;
		this.properties.onKeyInput({ key, owner: createUserName, project: name, parentId, appType });
	}

	private _onNameInput({ target: { value: pageName } }: WithTarget) {
		const {
			project: { createUserName, name },
			parentId,
			appType,
		} = this.properties;
		this.properties.onNameInput({ name: pageName, owner: createUserName, project: name, parentId, appType });
	}

	private _onDescriptionInput({ target: { value: description } }: WithTarget) {
		this.properties.onDescriptionInput({ description });
	}

	private _onSavePage() {
		const {
			project: { createUserName, name },
			parentGroups,
		} = this.properties;

		let parentPath = '';
		if (parentGroups.length > 0) {
			parentPath = parentGroups[parentGroups.length - 1].path.substring(1);
		}
		this.properties.onSavePage({ owner: createUserName, project: name, parentPath });
	}
}
