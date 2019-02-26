const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness';
import { w, v } from '@dojo/framework/widget-core/d';

import * as css from '../../../src/pages/project/NewProject.m.css';
import NewProject from '../../../src/pages/project/NewProject';

describe('NewProject', () => {
	it('default renders correctly', () => {
		const h = harness(() =>
			w(NewProject, {
				loggedUsername: 'user',
				loggedAvatarUrl: 'avatar',
				name: 'name',
				isPublic: true,
				onNameInput: () => {},
				onDescriptionInput: () => {},
				onIsPublicInput: () => {},
				onSaveProject: () => {}
			})
		);
		h.expect(() =>
			v('div', { classes: [css.root, 'container mt-5'] }, [
				v('div', [
					v('h2', ['创建项目']),
					v('small', { classes: ['form-text text-muted'] }, [
						'项目包含页面、页面分组、页面模板以及变更历史等。'
					]),
					v('hr')
				]),
				v('form', { classes: ['needs-validation'], novalidate: 'novalidate' }, [
					v('div', { classes: ['form-group'] }, [
						v('label', { for: 'projectName' }, [
							'名称',
							v('small', { classes: ['text-muted'] }, [' (必填)'])
						]),
						v('div', { classes: ['input-group'] }, [
							v('div', { classes: ['input-group-prepend'] }, [
								v('span', { classes: ['input-group-text'] }, [
									v(
										'img',
										{ classes: ['avatar mr-1'], src: 'avatar', width: 20, height: 20, alt: 'user' },
										[` user /`]
									)
								])
							]),
							v('input', {
								type: 'text',
								id: 'projectName',
								classes: ['form-control'],
								required: 'required',
								maxlength: '32',
								focus: true,
								oninput: () => {}
							})
						]),
						v('small', { classes: ['form-text text-muted'] }, [
							'项目名要简短易记。只允许字母、数字、中划线(-)、下划线(_)、点(.)等。如 ',
							v('strong', { classes: ['text-info'] }, ['hello-world']),
							'。'
						])
					]),
					v('div', { classes: ['form-group'] }, [
						v('label', { for: 'projectDesc' }, ['描述']),
						v('input', {
							type: 'text',
							classes: ['form-control'],
							id: 'projectDesc',
							maxlength: '64',
							oninput: () => {}
						})
					]),
					v('div', { classes: ['form-check'] }, [
						v('input', {
							classes: ['form-check-input'],
							type: 'radio',
							id: 'isPublic',
							value: 'true',
							name: 'isPublic',
							checked: true,
							onclick: () => {}
						}),
						v('label', { classes: ['form-check-label'], for: 'isPublic' }, ['公开']),
						v('small', { classes: ['form-text text-muted'] }, ['所有访客均可浏览，可邀请用户维护。'])
					]),
					v('div', { classes: ['form-check'] }, [
						v('input', {
							classes: ['form-check-input'],
							type: 'radio',
							id: 'isPrivate',
							value: 'false',
							name: 'isPublic',
							checked: false,
							onclick: () => {}
						}),
						v('label', { classes: ['form-check-label'], for: 'isPrivate' }, ['私有']),
						v('small', { classes: ['form-text text-muted'] }, ['仅成员可浏览，可邀请用户浏览和维护。'])
					]),
					v('hr'),
					v(
						'button',
						{
							type: 'button',
							classes: ['btn btn-primary'],
							disabled: true,
							onclick: () => {}
						},
						['创建']
					)
				])
			])
		);
	});
});
