import { create, tsx } from '@dojo/framework/core/vdom';

export interface NewWebProjectProperties {}

const factory = create().properties<NewWebProjectProperties>();

export default factory(function NewWebProject({ properties }) {
	const {} = properties();
	return <div>new web project</div>;
});
