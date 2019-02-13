import { v } from '@dojo/framework/widget-core/d';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { theme, ThemedMixin } from '@dojo/framework/widget-core/mixins/Themed';

import * as css from './styles/NewProject.m.css';
import messageBundle from './nls/main';

export interface NewProjectProperties {
	loggedUsername: string;
	loggedAvatarUrl: string;
}

@theme(css)
export default class NewProject extends ThemedMixin(I18nMixin(WidgetBase))<NewProjectProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [css.root, 'container mt-5'] }, [this._renderTitle(), this._renderInputForm()]);
	}

	private _renderTitle() {
		const { messages } = this._localizedMessages;
		return v('div', [
			v('h2', [messages.newProject]),
			v('small', { classes: ['form-text text-muted'] }, [messages.newProjectHelp]),
			v('hr')
		]);
	}

	private _renderInputForm() {
		const { loggedUsername, loggedAvatarUrl } = this.properties;

		return v('form', { classes: ['needs-validation'], novalidate: 'novalidate' }, [
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

		return v('div', { classes: ['form-group'] }, [
			v('label', { for: 'projectName' }, [
				messages.projectNameLabel,
				v('small', { classes: ['text-muted'] }, [` ${messages.requiredLabel}`])
			]),
			v('div', { classes: ['input-group'] }, [
				v('div', { classes: ['input-group-prepend'] }, [
					v('span', { classes: ['input-group-text'] }, [
						v(
							'img',
							{
								classes: ['avatar mr-1'],
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
					classes: ['form-control'],
					required: 'required',
					maxlength: '32',
					focus: true
				})
			]),
			v('small', { classes: ['form-text text-muted'] }, [
				messages.projectNameHelp,
				v('strong', { classes: ['text-info'] }, ['hello-world']),
				messages.dot
			])
		]);
	}

	private _renderDescriptionInput() {
		const { messages } = this._localizedMessages;

		return v(
			'div',
			{
				classes: ['form-group']
			},
			[
				v('label', { for: 'projectDesc' }, [messages.projectDescLabel]),
				v('input', {
					type: 'text',
					classes: ['form-control'],
					id: 'projectDesc',
					maxlength: '64'
				})
			]
		);
	}

	private _renderIsPublicCheckbox() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: ['form-check'] }, [
			v('input', { classes: ['form-check-input'], type: 'radio', id: 'isPublic', value: 'false', checked: true }),
			v('label', { classes: ['form-check-label'], for: 'isPublic' }, [messages.projectPublicLabel]),
			v('small', { classes: ['form-text text-muted'] }, [messages.projectPublicHelp])
		]);
	}

	private _renderIsPrivateCheckbox() {
		const { messages } = this._localizedMessages;

		return v('div', { classes: ['form-check'] }, [
			v('input', { classes: ['form-check-input'], type: 'radio', id: 'isPrivate', value: 'true' }),
			v('label', { classes: ['form-check-label'], for: 'isPrivate' }, [messages.projectPrivateLabel]),
			v('small', { classes: ['form-text text-muted'] }, [messages.projectPrivateHelp])
		]);
	}

	private _renderSaveButton() {
		const { messages } = this._localizedMessages;

		return v('button', { type: 'button', classes: ['btn btn-primary'], disabled: true }, [
			messages.projectSaveLabel
		]);
	}
}
