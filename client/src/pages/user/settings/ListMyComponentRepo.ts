//import messageBundle from '../../nls/main';
import * as c from '../../../className';
import * as css from './ListMyComponentRepo.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import { ListComponentRepoProperties } from '../../marketplace/ListComponentRepo';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v, w } from '@dojo/framework/widget-core/d';
import Link from '@dojo/framework/routing/Link';

@theme(css)
export default class ListMyComponentRepo extends ThemedMixin(I18nMixin(WidgetBase))<ListComponentRepoProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [css.root, c.container, c.mt_5] }, [
			v('div', { classes: [c.row] }, [
				v('div', { classes: [c.col_3] }, [
					v('ul', { classes: [c.list_group] }, [
						v('li', { classes: [c.list_group_item] }, [w(Link, { to: 'settings-profile' }, ['个人资料'])]),
						// TODO: 删除
						v('li', { classes: [c.list_group_item, css.active] }, ['组件市场'])
					])
				]),
				v('div', { classes: [c.col_9] }, [
					v('div', [
						v('div', { classes: [c.d_flex, c.justify_content_between, c.align_items_center] }, [
							v('span', { classes: [c.h4] }, ['组件市场']),
							w(Link, { to: 'new-component-repo', classes: [c.btn, c.btn_primary, c.btn_sm] }, [
								'发布组件'
							])
						]),
						v('hr')
					]),

					// 发布组件 form 表单
					// 点击按钮，或按下回车键提交
					// 校验 url 是否有效的 git 仓库地址
					// 1. 格式有效,支持 https 协议
					// 2. 属于公开仓库，能够访问
					v('form', { classes: [c.needs_validation], novalidate: 'novalidate' }, [
						v('div', { classes: [c.form_group] }, [
							v('div', { classes: [c.input_group] }, [
								v('input', {
									type: 'text',
									classes: [c.form_control],
									placeholder: 'HTTPS 协议的 git 仓库地址',
									'aria-label': 'git 仓库地址',
									'aria-describedby': 'btn-addon'
								}),
								v('div', { classes: [c.input_group_append] }, [
									v(
										'button',
										{
											classes: [c.btn, c.btn_outline_primary],
											type: 'button',
											id: 'btn-addon'
										},
										['发布']
									)
								])
							]),
							v('small', { classes: [c.form_text, c.text_muted] }, [
								'填写托管在 github 或码云等网站的仓库地址，敲回车或按“发布”按钮提交'
							])
						])
					])
				])
			])
		]);
	}
}
