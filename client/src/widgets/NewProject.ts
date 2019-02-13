import { v } from '@dojo/framework/widget-core/d';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';

import * as css from './styles/NewProject.m.css';

export interface NewProjectProperties {
	loggedUsername: string;
	loggedAvatarUrl: string;
}

export default class NewProject extends WidgetBase<NewProjectProperties> {
	protected render() {
		return v('div', { classes: [css.root, 'container mt-5'] }, [this._renderTitle(), this._renderInputForm()]);
	}

	private _renderTitle() {
		return v('div', [
			v('h2', ['创建项目']),
			v('small', { classes: ['form-text text-muted'] }, ['项目包含页面、页面分组、页面模板以及变更历史等。']),
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
		return v('div', { classes: ['form-group'] }, [
			v('label', { for: 'projectName' }, ['名称', v('small', { classes: ['text-muted'] }, [' (必填)'])]),
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
				'项目名要简短易记。只允许字母、数字、中划线(-)、下划线(_)、点(.)等。如 ',
				v('strong', { classes: ['text-info'] }, ['hello-world']),
				'。'
			])
		]);
	}

	private _renderDescriptionInput() {
		return v(
			'div',
			{
				classes: ['form-group']
			},
			[
				v('label', { for: 'projectDesc' }, ['描述']),
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
		return v('div', { classes: ['form-check'] }, [
			v('input', { classes: ['form-check-input'], type: 'radio', id: 'isPublic', value: 'false', checked: true }),
			v('label', { classes: ['form-check-label'], for: 'isPublic' }, ['公开']),
			v('small', { classes: ['form-text text-muted'] }, ['所有访客均可浏览，可邀请用户维护。'])
		]);
	}

	private _renderIsPrivateCheckbox() {
		return v('div', { classes: ['form-check'] }, [
			v('input', { classes: ['form-check-input'], type: 'radio', id: 'isPrivate', value: 'true' }),
			v('label', { classes: ['form-check-label'], for: 'isPrivate' }, ['私有']),
			v('small', { classes: ['form-text text-muted'] }, ['仅成员可浏览，可邀请用户浏览和维护。'])
		]);
	}

	private _renderSaveButton() {
		return v('button', { type: 'button', classes: ['btn btn-primary'], disabled: true }, ['创建']);
	}
}
