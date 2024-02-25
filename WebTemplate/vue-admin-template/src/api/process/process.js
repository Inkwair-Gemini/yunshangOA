import request from '@/utils/request'

const api_name = '/admin/process'

export default {

  getPageList(page, limit, searchObj) {
    return request({
      url: `${api_name}/${page}/${limit}`,
      method: 'get',
      params: searchObj // url查询字符串或表单键值对
    })
  },
  show(id) {
    return request({
      url: `${api_name}/show/`+id,
      method: 'get'
    })
  },
}
