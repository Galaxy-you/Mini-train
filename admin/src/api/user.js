import request from '@/utils/request'

/**
 * User management APIs
 */

/**
 * Get user list with pagination
 */
export function getUserList(params) {
  return request({
    url: '/api/admin/user',
    method: 'get',
    params
  })
}

/**
 * Get user detail by id
 */
export function getUserDetail(id) {
  return request({
    url: `/api/admin/user/${id}`,
    method: 'get'
  })
}

/**
 * Update user auth status
 */
export function updateAuthStatus(id, authStatus) {
  return request({
    url: `/api/admin/user/${id}/auth-status`,
    method: 'put',
    params: { authStatus }
  })
}

/**
 * Reset user password
 */
export function resetUserPassword(id) {
  return request({
    url: `/api/admin/user/${id}/reset-password`,
    method: 'post'
  })
}
