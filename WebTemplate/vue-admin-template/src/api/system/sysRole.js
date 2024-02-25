/*
角色管理相关的API请求函数
*/
import request from '@/utils/request'

const api_name = '/admin/system/sysRole'

export default {

 /**
  * 获取角色分页列表(带搜索)
  * @param {*} page 
  * @param {*} limit 
  * @param {*} searchObj 
  * @returns 
  */
  getPageList(page, limit, searchObj) {
    return request({
      url: `${api_name}/${page}/${limit}`,
      method: 'get',
      // 如果是普通对象参数写法，params:对象参数名
      // 如果是使用json格式传递，data:对象参数名
      params: searchObj
    })
  },

  /**
   * 角色删除
   * @param {*} id 
   * @returns 
   */
  removeById(id) {
    return request({
      url: `${api_name}/delete/${id}`,
      method: 'delete'
    })
  },

  /**
   * 角色添加
   * @param {*} role 
   * @returns 
   */
  save(role) {
    return request({
      url: `${api_name}/save`,
      method: 'post',
      data: role
    })
  },

  // 回显要修改的id信息
  getById(id) {
    return request({
      url: `${api_name}/get/${id}`,
      method: 'get'
    })
  },
  
  // 修改
  updateById(role) {
    return request({
      url: `${api_name}/update`,
      method: 'put',
      data: role
    })
  },

  // 批量删除
  batchRemove(idList) {
    return request({
      url: `${api_name}/ids`,
      method: `delete`,
      data: idList
    })
  },
  getRoles(adminId) {
    return request({
      url: `${api_name}/toAssign/${adminId}`,
      method: 'get'
    })
  },
  
  assignRoles(assginRoleVo) {
    return request({
      url: `${api_name}/doAssign`,
      method: 'post',
      data: assginRoleVo
    })
  }
}