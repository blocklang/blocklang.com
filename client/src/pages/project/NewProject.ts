import { v, w } from '@dojo/framework/widget-core/d';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as c from '../../className';
import * as css from './NewProject.m.css';
import messageBundle from '../../nls/main';
import { WithTarget } from '../../interfaces';
import { NamePayload, DescriptionPayload, IsPublicPayload } from '../../processes/interfaces';
import { ValidateStatus } from '../../constant';
import Exception from '../error/Exception';

export interface NewProjectProperties {
	// user
	loggedUsername: string;
	loggedAvatarUrl: string;
	// attr
	id?: number;
	name: string;
	description?: string;
	isPublic: boolean;
	// validation
	nameValidateStatus?: ValidateStatus;
	nameErrorMessage?: string;
	// event
	onNameInput: (opts: NamePayload) => void;
	onDescriptionInput: (opts: DescriptionPayload) => void;
	onIsPublicInput: (opts: IsPublicPayload) => void;
	onSaveProject: (opts: object) => void;
}

@theme(css)
export default class NewProject extends ThemedMixin(I18nMixin(WidgetBase))<NewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		debugger;
		if (!this._isAuthenticated()) {
			return w(Exception, { type: '403' });
		}
		return v('div', { classes: [css.root, c.container, c.mt_5] }, [this._renderTitle(), this._renderInputForm()]);
	}

	private _isAuthenticated() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderTitle() {
		const { messages } = this._localizedMessages;
		return v('div', [
			v('h2', [messages.newProject]),
			v('small', { classes: [c.form_text, c.text_muted] }, [messages.newProjectHelp]),
			v('hr')
		]);
	}

	private _renderInputForm() {
		const { loggedUsername, loggedAvatarUrl } = this.properties;

		return v('form', { classes: [c.needs_validation], novalidate: 'novalidate' }, [
			this._renderNameInput(loggedAvatarUrl, loggedUsername),
			this._renderDescriptionInput(),
			this._renderIsPublicCheckbox(),
			this._renderIsPrivateCheckbox(),
			v('hr'),
			this._renderSaveButton()
		]);
	}

	private _renderNameInput(loggedAvatarUrl: string, loggedUsername: string) {
		const { messages } = this._localizedMessages;
		const { nameValidateStatus = ValidateStatus.UNVALIDATED, nameErrorMessage } = this.properties;

		const inputClasses = [c.form_control];
		if (nameValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'projectName' }, [
				messages.projectNameLabel,
				v('small', { classes: [c.text_muted] }, [` ${messages.requiredLabel}`])
			]),
			v('div', { classes: [c.input_group] }, [
				v('div', { classes: [c.input_group_prepend] }, [
					v('span', { classes: [c.input_group_text] }, [
						v(
							'img',
							{
								classes: [c.avatar, c.mr_1],
								src: `${loggedAvatarUrl}`,
								width: 20,
								height: 20,
								alt: `${loggedUsername}`
							},
							[` ${loggedUsername} /`]
						)
					])
				]),
				v('input', {
					type: 'text',
					id: 'projectName',
					classes: inputClasses,
					required: 'required',
					maxlength: '32',
					focus: true,
					oninput: this._onNameInput
				}),
				nameValidateStatus === ValidateStatus.INVALID
					? v('div', { classes: [c.invalid_tooltip], innerHTML: `${nameErrorMessage}` })
					: null
			]),
			v('small', { classes: [c.form_text, c.text_muted] }, [
				messages.projectNameHelp,
				v('strong', { classes: [c.text_info] }, ['hello-world']),
				messages.dot
			])
		]);
	}

	private _renderDescriptionInput() {
		const { messages } = this._localizedMessages;

		return v(
			'div',
			{
				classes: [c.form_group]
			},
			[
				v('label', { for: 'projectDesc' }, [messages.projectDescLabel]),
				v('input', {
					type: 'text',
					classes: [c.form_control],
					id: 'projectDesc',
					maxlength: '64',
					oninput: this._onDescriptionInput
				})
			]
		);
	}

	private _renderIsPublicCheckbox() {
		const { messages } = this._localizedMessages;
		const { isPublic = true } = this.properties;

		return v('div', { classes: [c.form_check] }, [
			v('input', {
				classes: [c.form_check_input],
				type: 'radio',
				id: 'isPublic',
				value: 'true',
				checked: isPublic,
				name: 'isPublic',
				onclick: this._onIsPublicInput
			}),
			v('label', { classes: [c.form_check_label], for: 'isPublic' }, [messages.projectPublicLabel]),
			v('small', { classes: [c.form_text, c.text_muted] }, [messages.projectPublicHelp])
		]);
	}

	private _renderIsPrivateCheckbox() {
		const { messages } = this._localizedMessages;
		const { isPublic = true } = this.properties;

		return v('div', { classes: [c.form_check] }, [
			v('input', {
				classes: [c.form_check_input],
				type: 'radio',
				id: 'isPrivate',
				value: 'false',
				checked: !isPublic,
				name: 'isPublic',
				onclick: this._onIsPublicInput
			}),
			v('label', { classes: [c.form_check_label], for: 'isPrivate' }, [messages.projectPrivateLabel]),
			v('small', { classes: [c.form_text, c.text_muted] }, [messages.projectPrivateHelp])
		]);
	}

	private _renderSaveButton() {
		const { messages } = this._localizedMessages;
		const { nameValidateStatus = ValidateStatus.UNVALIDATED } = this.properties;

		const disabled = nameValidateStatus === ValidateStatus.VALID ? false : true;

		return v(
			'button',
			{
				type: 'button',
				classes: [c.btn, c.btn_primary],
				disabled,
				onclick: disabled ? undefined : this._onSaveProject
			},
			[messages.projectSaveLabel]
		);
	}

	private _onNameInput({ target: { value: name } }: WithTarget) {
		this.properties.onNameInput({ name });
	}

	private _onDescriptionInput({ target: { value: description } }: WithTarget) {
		this.properties.onDescriptionInput({ description });
	}

	private _onSaveProject() {
		this.properties.onSaveProject({});
	}

	private _onIsPublicInput(event: MouseEvent) {
		const radio = event.target as HTMLInputElement;
		const { value, checked } = radio;
		let isPublic = true;
		if (checked) {
			isPublic = value === 'true' ? true : false;
		} else {
			isPublic = value === 'true' ? false : true;
		}
		this.properties.onIsPublicInput({ isPublic });
	}
}
