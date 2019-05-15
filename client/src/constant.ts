export enum ValidateStatus {
	UNVALIDATED,
	VALID,
	INVALID
}

export enum ResourceType {
	Page = '01',
	Group = '02',
	Pane = '03',
	PageTemplet = '04',
	File = '05',
	Service = '06'
}

export enum GitFileStatus {
	Untracked = '01',
	Added = '02',
	Changed = '03',
	Removed = '04',
	Deleted = '05',
	Missing = '06',
	Modified = '07',
	Conflicting = '08'
}

export enum ReleaseResult {
	Inited = '01',
	Started = '02',
	Failed = '03',
	Passed = '04',
	Canceled = '05'
}

export enum AccessLevel {
	Forbidden = '01',
	Read = '02',
	Write = '03',
	Admin = '04'
}
