import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';

import messageBundle from '../../../nls/main';
import * as c from '@blocklang/bootstrap-classes';
import * as css from './Profile.m.css';
import ThemedMixin, { theme } from '@dojo/framework/core/mixins/Themed';
import I18nMixin from '@dojo/framework/core/mixins/I18n';
import { WithTarget, ProfileInfo } from '../../../interfaces';
import {
	NicknamePayload,
	WebsiteUrlPayload,
	CompanyPayload,
	LocationPayload,
	BioPayload,
} from '../../../processes/interfaces';
import Exception from '../../error/Exception';
import Link from '@dojo/framework/routing/Link';

export interface ProfileProperties {
	loggedUsername: string;
	profileUpdateSuccessMessage?: string;
	profile: ProfileInfo;
	onNicknameInput: (opts: NicknamePayload) => void;
	onWebsiteUrlInput: (opts: WebsiteUrlPayload) => void;
	onCompanyInput: (opts: CompanyPayload) => void;
	onLocationInput: (opts: LocationPayload) => void;
	onBioInput: (opts: BioPayload) => void;
	onUpdateProfile: (opts: object) => void;
	onCloseSuccessAlert: (opts: object) => void;
}

@theme(css)
export default class Profile extends ThemedMixin(I18nMixin(WidgetBase))<ProfileProperties> {
	private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		if (!this._isAuthenticated()) {
			return w(Exception, { type: '403' });
		}

		return v('div', { classes: [css.root, c.container, c.mt_5] }, [
			v('div', { classes: [c.row] }, [
				v('div', { classes: [c.col_3] }, [this._renderMenu()]),
				v('div', { classes: [c.col_9] }, [this._renderHeader(), this._renderEditProfileForm()]),
			]),
		]);
	}

	private _renderEditProfileForm() {
		const {
			profile: { email },
			profileUpdateSuccessMessage,
		} = this.properties;

		return v('form', { classes: [c.needs_validation], novalidate: true }, [
			this._renderNickname(),
			email ? this._renderEmail() : null,
			this._renderWebsiteUrl(),
			this._renderCompany(),
			this._renderLocation(),
			this._renderBio(),
			v('hr'),
			this._renderSaveButton(),
			profileUpdateSuccessMessage ? this._renderSaveSuccessTip() : null,
		]);
	}

	private _renderHeader() {
		const { messages } = this._localizedMessages;
		return v('div', [v('h4', [`${messages.publicProfile}`]), v('hr')]);
	}

	private _renderMenu() {
		const { messages } = this._localizedMessages;
		return v('ul', { classes: [c.list_group] }, [
			v('li', { classes: [c.list_group_item, css.active] }, [`${messages.userSettingMenuProfile}`]),
			v('li', { classes: [c.list_group_item] }, [
				w(Link, { to: 'settings-marketplace' }, [`${messages.userSettingMenuMarketplace}`]),
			]),
		]);
	}

	private _isAuthenticated() {
		const { loggedUsername } = this.properties;
		return !!loggedUsername;
	}

	private _renderSaveSuccessTip() {
		const { profileUpdateSuccessMessage } = this.properties;

		return v(
			'div',
			{ classes: [c.alert, c.alert_success, c.alert_dismissible, c.fade, c.show, c.mt_2], role: 'alert' },
			[
				`${profileUpdateSuccessMessage}`,
				v(
					'button',
					{
						type: 'button',
						classes: [c.close],
						'data-dismiss': 'alert',
						'aria-label': 'Close',
						onclick: this._closeSuccessAlert,
					},
					[v('span', { 'aria-hidden': 'true', innerHTML: '&times;' })]
				),
			]
		);
	}

	private _renderNickname() {
		const {
			profile: { nickname = '' },
		} = this.properties;
		const { messages } = this._localizedMessages;
		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'nickname' }, [`${messages.nickName}`]),
			v('input', {
				type: 'text',
				id: 'nickname',
				classes: [c.form_control],
				maxlength: 32,
				focus: true,
				value: `${nickname}`,
				oninput: this._onNicknameInput,
			}),
		]);
	}

	private _renderEmail() {
		const {
			profile: { email },
		} = this.properties;
		const { messages } = this._localizedMessages;
		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'email' }, [`${messages.email}`]),
			v('input', {
				type: 'text',
				id: 'email',
				readOnly: true,
				classes: [c.form_control_plaintext],
				maxlength: 64,
				value: `${email}`,
			}),
			v('small', { classes: [c.form_text, c.text_muted] }, [`${messages.emailHelpText}`]),
		]);
	}

	private _renderWebsiteUrl() {
		const {
			profile: { websiteUrl = '' },
		} = this.properties;
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'websiteUrl' }, [`${messages.websiteUrl}`]),
			v('input', {
				type: 'text',
				classes: [c.form_control],
				id: 'websiteUrl',
				maxlength: 128,
				value: `${websiteUrl}`,
				oninput: this._onWebsiteUrlInput,
			}),
			v('small', { classes: [c.form_text, c.text_muted] }, [`${messages.websiteUrlHelpText}`]),
		]);
	}

	private _renderCompany() {
		const {
			profile: { company = '' },
		} = this.properties;
		const { messages } = this._localizedMessages;

		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'company' }, [`${messages.company}`]),
			v('input', {
				type: 'text',
				classes: [c.form_control],
				id: 'company',
				maxlength: 64,
				value: `${company}`,
				oninput: this._onCompanyInput,
			}),
		]);
	}

	private _renderLocation() {
		const {
			profile: { location = '' },
		} = this.properties;
		const { messages } = this._localizedMessages;
		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'location' }, [`${messages.location}`]),
			v('input', {
				type: 'text',
				classes: [c.form_control],
				id: 'location',
				maxlength: 128,
				value: `${location}`,
				oninput: this._onLocationInput,
			}),
		]);
	}

	private _renderBio() {
		const {
			profile: { bio = '' },
		} = this.properties;
		const { messages } = this._localizedMessages;
		return v('div', { classes: [c.form_group] }, [
			v('label', { for: 'bio' }, [`${messages.bio}`]),
			v('textarea', {
				classes: [c.form_control],
				id: 'bio',
				maxlength: 256,
				value: `${bio}`,
				oninput: this._onBioInput,
			}),
		]);
	}

	private _renderSaveButton() {
		const { messages } = this._localizedMessages;
		return v(
			'button',
			{
				type: 'button',
				classes: [c.btn, c.btn_primary],
				onclick: this._onUpdateProfile,
			},
			[`${messages.userSettingSaveButton}`]
		);
	}

	private _onNicknameInput({ target: { value: nickname } }: WithTarget) {
		this.properties.onNicknameInput({ nickname });
	}

	private _onWebsiteUrlInput({ target: { value: websiteUrl } }: WithTarget) {
		this.properties.onWebsiteUrlInput({ websiteUrl });
	}

	private _onCompanyInput({ target: { value: company } }: WithTarget) {
		this.properties.onCompanyInput({ company });
	}

	private _onLocationInput({ target: { value: location } }: WithTarget) {
		this.properties.onLocationInput({ location });
	}

	private _onBioInput({ target: { value: bio } }: WithTarget) {
		this.properties.onBioInput({ bio });
	}

	private _onUpdateProfile() {
		this.properties.onUpdateProfile({});
	}

	private _closeSuccessAlert(event: MouseEvent) {
		event.stopPropagation(); // 禁用 bootstrap 的关闭 alert 事件
		this.properties.onCloseSuccessAlert({});
	}
}
