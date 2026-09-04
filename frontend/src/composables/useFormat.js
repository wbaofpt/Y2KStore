export function formatPrice(val) {
  if (val === undefined || val === null) return '0đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

export const orderStatusMap = {
  PROCESSING: { label: 'Chờ xử lý', class: 'bg-warning text-dark' },
  PREPARING: { label: 'Đang chuẩn bị', class: 'bg-info text-dark' },
  SHIPPED: { label: 'Đang vận chuyển', class: 'bg-primary text-white' },
  DELIVERED: { label: 'Đã giao', class: 'bg-success text-white' },
  CANCELLED: { label: 'Đã hủy', class: 'bg-danger text-white' }
}
