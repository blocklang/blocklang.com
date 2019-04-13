import * as object from '@dojo/framework/shim/object';

/**
 * 确认 json 对象是否为空，即等于 `{}`
 *
 * @param obj json 对象
 */
export function isEmpty(obj: any): boolean {
	return object.entries(obj).length === 0 && obj.constructor === Object;
}
