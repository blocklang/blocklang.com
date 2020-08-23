import has from '@dojo/framework/core/has';
// 开发环境下，设置为 http://localhost:3000
// 生产环境下，设置为空字符串
export const baseUrl = has('production') ? '' : 'http://localhost:3000';
