import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';

import messageBundle from '../../nls/main';
import * as c from '../../className';
import * as css from './Setting.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { LoginNamePayload } from '../../processes/interfaces';
import { ValidateStatus } from '../../constant';
import { WithTarget } from '../../interfaces';
import Exception from '../error/Exception';

export interface CompleteUserInfoProperties {
	// attr
	avatarUrl: string;
	nickname: string;
	loginName: string;
	// validation
	loginNameValidateStatus?: ValidateStatus;
	loginNameErrorMessage?: string;

	// event
	onLoginNameInput: (opts: LoginNamePayload) => void;
	onUpdateUserInfo: (opts: object) => void;
}

@theme(css)
export default class CompleteUserInfo extends ThemedMixin(I18nMixin(WidgetBase))<CompleteUserInfoProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	private _isAuthenticated() {
		const { nickname } = this.properties;

		// nickname 肯定有值，如果没有值，则不应显示完善用户信息页面
		return nickname.trim() !== '';
	}

	protected render() {
		if (!this._isAuthenticated()) {
			return w(Exception, { type: '403' });
		}

		const { messages } = this._localizedMessages;
		const {
			avatarUrl = '',
			nickname = '',
			loginName = '',
			loginNameValidateStatus,
			loginNameErrorMessage
		} = this.properties;

		const disabled = loginNameValidateStatus === ValidateStatus.VALID ? false : true;

		const inputClasses = [c.form_control];
		if (loginNameValidateStatus === ValidateStatus.INVALID) {
			inputClasses.push(c.is_invalid);
		}

		return v('div', { classes: [c.container, c.mt_5], styles: { maxWidth: '700px' } }, [
			v('div', [
				v('h4', [
					v('img', { classes: [c.avatar], src: `${avatarUrl}`, width: 20, height: 20 }),
					` ${nickname} ${messages.completeUserInfoTip}`
				]),
				v('hr')
			]),
			v('form', { classes: [c.needs_validation], novalidate: true }, [
				v('div', { classes: [c.form_group] }, [
					v('label', { for: 'loginName' }, [`${messages.loginName}`]),
					v('div', { classes: [c.input_group] }, [
						v('input', {
							type: 'text',
							id: 'loginName',
							maxlength: 32,
							focus: true,
							classes: inputClasses,
							value: `${loginName}`,
							oninput: this._onLoginNameInput
						}),
						loginNameValidateStatus === ValidateStatus.INVALID
							? v('div', { classes: [c.invalid_tooltip], innerHTML: `${loginNameErrorMessage}` })
							: null
					]),
					v('small', { classes: [c.form_text, c.text_muted] }, [`${messages.loginNameHelpText}`])
				]),
				v('hr'),
				v(
					'button',
					{
						type: 'button',
						classes: [c.btn, c.btn_primary],
						disabled,
						onclick: disabled ? undefined : this._onUpdateUserInfo
					},
					[`${messages.ok}`]
				)
			])
		]);
	}

	private _onLoginNameInput({ target: { value: loginName } }: WithTarget) {
		this.properties.onLoginNameInput({ loginName });
	}

	private _onUpdateUserInfo() {
		this.properties.onUpdateUserInfo({});
	}
}
