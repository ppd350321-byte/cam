import request from '@/api/request';

/**
 * 上传图片到 Appwrite Storage（通过后端代理）
 * @param {File} file 图片文件
 * @returns {Promise<{url: string}>} 返回图片 URL
 */
export function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: '/admin/upload/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  });
}
