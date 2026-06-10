import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 用户注册
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 获取工作台统计数据
 */
export function getDashboardStats() {
    return request({
        url: '/dashboard/stats',
        method: 'get'
    })
}

/**
 * 获取所有数据源
 */
export function getDataSources() {
    return request({
        url: '/datasources',
        method: 'get'
    })
}

/**
 * 获取数据源详情
 */
export function getDataSource(id) {
    return request({
        url: `/datasources/${id}`,
        method: 'get'
    })
}

/**
 * 创建数据源
 */
export function createDataSource(data) {
    return request({
        url: '/datasources',
        method: 'post',
        data
    })
}

/**
 * 更新数据源
 */
export function updateDataSource(id, data) {
    return request({
        url: `/datasources/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除数据源
 */
export function deleteDataSource(id) {
    return request({
        url: `/datasources/${id}`,
        method: 'delete'
    })
}

/**
 * 测试数据源连接
 */
export function testConnection(id) {
    return request({
        url: `/datasources/${id}/test`,
        method: 'post'
    })
}

/**
 * 获取所有数据表
 */
export function getAllTables() {
    return request({
        url: '/metadata/tables',
        method: 'get'
    })
}

/**
 * 根据数据源获取表列表
 */
export function getTablesBySource(sourceId) {
    return request({
        url: `/metadata/tables/source/${sourceId}`,
        method: 'get'
    })
}

/**
 * 搜索数据表
 */
export function searchTables(keyword) {
    return request({
        url: '/metadata/tables/search',
        method: 'get',
        params: { keyword }
    })
}

/**
 * 获取表详情
 */
export function getTableDetail(tableId) {
    return request({
        url: `/metadata/tables/${tableId}`,
        method: 'get'
    })
}

/**
 * 创建数据表
 */
export function createTable(data) {
    return request({
        url: '/metadata/tables',
        method: 'post',
        data
    })
}

/**
 * 更新数据表
 */
export function updateTable(id, data) {
    return request({
        url: `/metadata/tables/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除数据表
 */
export function deleteTable(id) {
    return request({
        url: `/metadata/tables/${id}`,
        method: 'delete'
    })
}

/**
 * 保存表列信息
 */
export function saveColumns(tableId, data) {
    return request({
        url: `/metadata/tables/${tableId}/columns`,
        method: 'post',
        data
    })
}

/**
 * 获取所有血缘关系
 */
export function getAllLineages() {
    return request({
        url: '/lineage',
        method: 'get'
    })
}

/**
 * 创建血缘关系
 */
export function createLineage(data) {
    return request({
        url: '/lineage',
        method: 'post',
        data
    })
}

/**
 * 删除血缘关系
 */
export function deleteLineage(id) {
    return request({
        url: `/lineage/${id}`,
        method: 'delete'
    })
}

/**
 * 获取血缘图谱
 */
export function getLineageGraph(tableId) {
    return request({
        url: `/lineage/graph/${tableId}`,
        method: 'get'
    })
}

/**
 * 搜索血缘关系
 */
export function searchLineage(tableName) {
    return request({
        url: '/lineage/search',
        method: 'get',
        params: { tableName }
    })
}
