import { v, w } from '@dojo/framework/core/vdom';
import WidgetBase from '@dojo/framework/core/WidgetBase';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/core/mixins/Themed';

import * as c from '@blocklang/bootstrap-classes';
import * as css from './NewRelease.m.css';
import messageBundle from '../../nls/main';
import ProjectHeader from '../widgets/ProjectHeader';
import { Project, JdkInfo, WithTarget } from '../../interfaces';
import Link from '@dojo/framework/routing/Link';
import { ValidateStatus } from '../../constant';
import { VersionPayload, DescriptionPayload, JdkReleaseIdPayload, TitlePayload } from '../../processes/interfaces';
import Exception from '../error/Exception';

export interface NewReleaseProperties {
	loggedUsername: string;
	project: Project;
	jdks: JdkInfo[];
	// attr
	id?: number;
	version: string;
	jdkReleaseId: number;
	title: string;
	description?: string;
	// validation
	versionValidateStatus?: ValidateStatus;
	versionErrorMessage?: string;
	titleValidateStatus?: ValidateStatus;
	titleErrorMessage?: string;
	// event
	onVersionInput: (opts: VersionPayload) => void;
	onJdkReleaseIdInput: (opts: JdkReleaseIdPayload) => void;
	onTitleInput: (opts: TitlePayload) => void;
	onDescriptionInput: (opts: DescriptionPayload) => void;
	onSaveReleaseTask: (opts: VersionPayload) => void;
}

@theme(css)
export default class NewRelease extends ThemedMixin(I18nMixin(WidgetBase))<NewReleaseProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		// TODO: 除了登录，还要判断用户是否有写的权限
		if (!this._isAuthenticated()) {
			return w(Exception, { type: '403' });
		}
		return v('div', { classes: [css.root, c.container] }, [
			this._renderHeader(),
			v('div', { classes: [c.container], styles: { maxWidth: '700px' } }, [
				this._renderNavigate(),
				this._renderReleaseForm(),
			]),
		]);
	}

	private _isAuthenticated() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderHeader() {
		const {
			messages: { privateProjectTitle },
		} = this._localizedMessages;
		const { project } = this.properties;

		return w(ProjectHeader, { project, privateProjectTitle });
	}

	private _renderNavigate() {
		const {
			messages: { releaseText },
		} = this._localizedMessages;
		const {
			project: { createUserName, name },
		} = this.properties;
		return v('div', { classes: [c.pb_4, c.mb_4, c.border_bottom] }, [
			w(
				Link,
				{ classes: [c.btn, c.btn_info], to: 'list-release', params: { owner: createUserName, project: name } },
				[`${releaseText}`]
			),
		]);
	}

	private _renderReleaseForm() {
		return v('div', { classes: [] }, [
			v('form', { classes: [c.needs_validation], novalidate: 'novalidate' }, [
				this._renderVersionInput(),
				this._renderTitleInput(),
				this._renderDescriptionTextarea(),
				v('hr'),
				this._renderJdkSelect(),
				v('hr'),
				this._renderSaveButton(),
			]),
		]);
	}

	private _renderVersionInput() {
		const { versionValidateStatus = ValidateStatus.UNVALIDATED, versionErrorMessage } = this.properties;

		const inputClasses = [c.form_control];
		if (versionValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}

		return v('div', { classes: [c.form_group] }, [
			v('div', { classes: [c.input_group] }, [
				v('div', { classes: [c.input_group_prepend] }, [v('span', { classes: [c.input_group_text] }, ['v'])]),
				v('input', {
					classes: inputClasses,
					type: 'text',
					focus: true,
					maxlength: '32',
					required: true,
					placeholder: '版本号，如 0.1.0',
					oninput: this._onVersionInput,
				}),
				versionValidateStatus === ValidateStatus.INVALID
					? v('div', { classes: [c.invalid_tooltip], innerHTML: `${versionErrorMessage}` })
					: null,
			]),
			v('small', { classes: [c.form_text, c.text_muted] }, [
				'请使用',
				v('a', { href: 'https://semver.org/lang/zh-CN/', target: '_blank', tabIndex: -1 }, ['语义化版本']),
				'。如用于生产环境为',
				v('strong', { classes: [c.text_info] }, ['0.1.0']),
				'，非生产环境为',
				v('strong', { classes: [c.text_info] }, ['0.1.0-alpha']),
			]),
		]);
	}

	private _renderJdkSelect() {
		const { jdks = [] } = this.properties;
		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'selJdk' }, ['JDK']),
			v(
				'select',
				{
					classes: [c.form_control],
					id: 'selJdk',
					onchange: this._onJdkSelect,
				},
				jdks.map((jdkInfo) => {
					return v('option', { id: `${jdkInfo.id}` }, [`${jdkInfo.name} ${jdkInfo.version}`]);
				})
			),
			v('small', { classes: [c.form_text, c.text_muted] }, [`发布的项目运行在 JDK 上，建议选择最新版 JDK`]),
		]);
	}

	private _renderTitleInput() {
		const { titleValidateStatus = ValidateStatus.UNVALIDATED, titleErrorMessage } = this.properties;

		const inputClasses = [c.form_control];
		if (titleValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}
		const {
			messages: { requiredLabel, releaseTitle },
		} = this._localizedMessages;

		return v('div', { classes: [c.form_group, c.position_relative] }, [
			v('label', { for: 'txtTitle' }, [
				`${releaseTitle}`,
				v('small', { classes: [c.text_muted] }, [` ${requiredLabel}`]),
			]),
			v('input', {
				type: 'text',
				maxlength: '32',
				id: 'txtTitle',
				required: true,
				classes: inputClasses,
				oninput: this._onTitleInput,
			}),
			titleValidateStatus === ValidateStatus.INVALID
				? v('div', { classes: [c.invalid_tooltip], innerHTML: `${titleErrorMessage}` })
				: null,
		]);
	}

	private _renderDescriptionTextarea() {
		const {
			messages: { releaseDescription },
		} = this._localizedMessages;

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'txtDescription' }, [`${releaseDescription}`]),
			v('textarea', {
				classes: [c.form_control],
				id: 'txtDescription',
				rows: '5',
				oninput: this._onDescriptionInput,
			}),
		]);
	}

	private _renderSaveButton() {
		const {
			messages: { releaseLabel },
		} = this._localizedMessages;

		const {
			versionValidateStatus = ValidateStatus.UNVALIDATED,
			titleValidateStatus = ValidateStatus.UNVALIDATED,
		} = this.properties;
		const disabled =
			versionValidateStatus === ValidateStatus.VALID && titleValidateStatus === ValidateStatus.VALID
				? false
				: true;

		return v(
			'button',
			{
				type: 'button',
				classes: [c.btn, c.btn_primary],
				disabled,
				onclick: disabled ? undefined : this._onSaveReleaseTask,
			},
			[`${releaseLabel}`]
		);
	}

	private _onVersionInput({ target: { value: version } }: WithTarget) {
		const {
			project: { createUserName, name },
		} = this.properties;
		this.properties.onVersionInput({ version, owner: createUserName, project: name });
	}

	private _onJdkSelect({ target: { value: jdkReleaseId } }: WithTarget) {
		this.properties.onJdkReleaseIdInput({ jdkReleaseId: Number(jdkReleaseId) });
	}

	private _onTitleInput({ target: { value: title } }: WithTarget) {
		this.properties.onTitleInput({ title });
	}

	private _onDescriptionInput({ target: { value: description } }: WithTarget) {
		this.properties.onDescriptionInput({ description });
	}

	private _onSaveReleaseTask() {
		const {
			project: { createUserName, name },
			version,
		} = this.properties;
		this.properties.onSaveReleaseTask({ owner: createUserName, project: name, version });
	}
}
