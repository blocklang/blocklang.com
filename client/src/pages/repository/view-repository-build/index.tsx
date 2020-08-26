import { tsx, create } from '@dojo/framework/core/vdom';
import i18n from '@dojo/framework/core/middleware/i18n';
import store from '../../../store';
import bundle from './nls/ViewRepositoryBuild';
import RepositoryHeader from '../../widgets/RepositoryHeader';
import { LoginStatus } from '../../../constant';
import Exception from '../../error/Exception';

export interface ViewRepositoryBuildProperties {
	owner: string;
	repository: string;
}

const factory = create({ store, i18n }).properties<ViewRepositoryBuildProperties>();

export default factory(function ViewRepositoryBuild({ properties, middleware: { store, i18n } }) {
	const { messages } = i18n.localize(bundle);
	const { get, path, executor } = store;
	const { owner, repository } = properties();

	const { status: loginStatus } = get(path('user'));
	if (loginStatus !== LoginStatus.LOGINED) {
		return <Exception type="403" />;
	}

	// TODO: 正在加载

	return (
		<div>
			<RepositoryHeader />
		</div>
	);
});
