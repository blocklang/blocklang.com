import { create, tsx } from '@dojo/framework/core/vdom';

export interface NewMiniProgramProperties {}

const factory = create().properties<NewMiniProgramProperties>();

export default factory(function NewMiniProgram({ properties }) {
	const {} = properties();
	return <div>new mini program</div>;
});
