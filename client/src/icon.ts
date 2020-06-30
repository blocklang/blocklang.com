import { library } from '@fortawesome/fontawesome-svg-core';

import { faGithub } from '@fortawesome/free-brands-svg-icons/faGithub';
import { faQq } from '@fortawesome/free-brands-svg-icons/faQq';
import { faFirefox } from '@fortawesome/free-brands-svg-icons/faFirefox';
import { faAndroid } from '@fortawesome/free-brands-svg-icons/faAndroid';
import { faApple } from '@fortawesome/free-brands-svg-icons/faApple';
import { faWeixin } from '@fortawesome/free-brands-svg-icons/faWeixin';
import { faAlipay } from '@fortawesome/free-brands-svg-icons/faAlipay';
import { faJava } from '@fortawesome/free-brands-svg-icons/faJava';
import { faGitAlt } from '@fortawesome/free-brands-svg-icons/faGitAlt';

import { faFolder } from '@fortawesome/free-solid-svg-icons/faFolder';
import { faSquare } from '@fortawesome/free-solid-svg-icons/faSquare';
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus';
import { faBookOpen } from '@fortawesome/free-solid-svg-icons/faBookOpen';
import { faEdit } from '@fortawesome/free-solid-svg-icons/faEdit';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons/faInfoCircle';
import { faLock } from '@fortawesome/free-solid-svg-icons/faLock';
import { faSignOutAlt } from '@fortawesome/free-solid-svg-icons/faSignOutAlt';
import { faHome } from '@fortawesome/free-solid-svg-icons/faHome';
import { faNewspaper } from '@fortawesome/free-solid-svg-icons/faNewspaper';
import { faPlug } from '@fortawesome/free-solid-svg-icons/faPlug';
import { faTag } from '@fortawesome/free-solid-svg-icons/faTag';
import { faClock } from '@fortawesome/free-solid-svg-icons/faClock';
import { faSpinner } from '@fortawesome/free-solid-svg-icons/faSpinner';
import { faTimes } from '@fortawesome/free-solid-svg-icons/faTimes';
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck';
import { faBan } from '@fortawesome/free-solid-svg-icons/faBan';
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog';
import { faUser } from '@fortawesome/free-solid-svg-icons/faUser';
import { faCodeBranch } from '@fortawesome/free-solid-svg-icons/faCodeBranch';
import { faCopy } from '@fortawesome/free-solid-svg-icons/faCopy';
import { faMinus } from '@fortawesome/free-solid-svg-icons/faMinus';
import { faSearch } from '@fortawesome/free-solid-svg-icons/faSearch';
import { faLightbulb } from '@fortawesome/free-solid-svg-icons/faLightbulb';
import { faPuzzlePiece } from '@fortawesome/free-solid-svg-icons/faPuzzlePiece';
import { faCube } from '@fortawesome/free-solid-svg-icons/faCube';
import { faPlayCircle } from '@fortawesome/free-solid-svg-icons/faPlayCircle';

export function init(): void {
	library.add(
		faGithub,
		faQq,
		faFirefox,
		faAndroid,
		faApple,
		faWeixin,
		faAlipay,
		faJava,
		faGitAlt,

		faPlus,
		faBookOpen,
		faEdit,
		faInfoCircle,
		faLock,
		faSignOutAlt,
		faHome,
		faFolder,
		faSquare,
		faNewspaper,
		faPlug,
		faTag,
		faClock,
		faSpinner,
		faTimes,
		faCheck,
		faBan,
		faCog,
		faUser,
		faCodeBranch,
		faCopy,
		faMinus,
		faSearch,
		faLightbulb,
		faPuzzlePiece,
		faCube,
		faPlayCircle
	);
}
