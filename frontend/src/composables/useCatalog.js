export function slugifyProduct(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'd')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '') || 'san-pham'
}

export function productPath(product) {
  return `/product/${slugifyProduct(product?.name ?? product?.productName)}`
}

export function toNumber(value, fallback = 0) {
  if (value === undefined || value === null || value === '') return fallback
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

export function normalizeProductStatus(value) {
  if (value === true) return 1
  if (value === false) return 4
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 1
}

export function isSellingStatus(value) {
  return normalizeProductStatus(value) === 1
}

export function isNewProduct(product, days = 14) {
  const createdAt = product?.createdAt ?? product?.created_at ?? product?.productDate ?? null
  if (!createdAt) return false
  const createdTime = new Date(createdAt).getTime()
  if (!Number.isFinite(createdTime)) return false
  return Date.now() - createdTime <= days * 24 * 60 * 60 * 1000
}

export function normalizeCategory(raw = {}) {
  const category = raw.category || {}
  const parentCategory = raw.parentCategory || category.parentCategory || {}

  return {
    ...raw,
    id: raw.id ?? raw.categoryId ?? category.id ?? null,
    name: raw.name ?? raw.categoryName ?? category.name ?? 'Danh mục',
    description: raw.description ?? category.description ?? '',
    status: raw.status ?? category.status ?? true,
    categoryType: normalizeCategoryType(raw.categoryType ?? category.categoryType),
    parentCategoryId: raw.parentCategoryId ?? category.parentCategoryId ?? parentCategory.id ?? null,
    parentCategoryName: raw.parentCategoryName ?? category.parentCategoryName ?? parentCategory.name ?? '',
    productCount: raw.productCount ?? 0
  }
}

function normalizeCategoryType(value) {
  return String(value || 'GENERAL').trim().toUpperCase() === 'TAG' ? 'TAG' : 'GENERAL'
}

export function normalizeProduct(raw = {}) {
  const category = raw.category || {}
  const rawCategoryIds = Array.isArray(raw.categoryIds)
    ? raw.categoryIds
    : Array.isArray(raw.categories)
      ? raw.categories.map((item) => item.id ?? item.categoryId).filter(Boolean)
      : []
  const rawCategoryNames = Array.isArray(raw.categoryNames)
    ? raw.categoryNames
    : Array.isArray(raw.categories)
      ? raw.categories.map((item) => item.name ?? item.categoryName).filter(Boolean)
      : []
  const variants = Array.isArray(raw.variants)
    ? raw.variants
    : Array.isArray(raw.productVariants)
      ? raw.productVariants
      : []

  const statusCode = normalizeProductStatus(raw.status ?? raw.isActive)
  const stockFromVariants = variants.reduce((sum, variant) => sum + toNumber(variant.stock, 0), 0)
  const stock = raw.stock ?? (variants.length ? stockFromVariants : null)
  const imageUrl = raw.imageUrl ?? raw.image ?? raw.thumbnail ?? category.imageUrl ?? ''
  const images = Array.isArray(raw.images)
    ? raw.images.map((image) => ({
        ...image,
        id: image.id ?? image.imageId ?? null,
        imageUrl: image.imageUrl ?? image.image ?? ''
      }))
    : []
  const normalizedVariants = variants.map((variant) => ({
    ...variant,
    id: variant.id ?? variant.variantId ?? null,
    productId: variant.productId ?? raw.id ?? raw.productId ?? null,
    size: variant.size ?? '',
    color: variant.color ?? '',
    stock: toNumber(variant.stock, 0),
    price: toNumber(variant.price ?? raw.price, 0),
    status: variant.status ?? true
  }))
  const minVariantPrice = normalizedVariants.length
    ? Math.min(...normalizedVariants.map((variant) => toNumber(variant.price, raw.price ?? 0)))
    : toNumber(raw.price, 0)
  const maxVariantPrice = normalizedVariants.length
    ? Math.max(...normalizedVariants.map((variant) => toNumber(variant.price, raw.price ?? 0)))
    : toNumber(raw.price, 0)

  return {
    ...raw,
    id: raw.id ?? raw.productId ?? null,
    name: raw.name ?? raw.productName ?? 'Sản phẩm',
    description: raw.description ?? '',
    price: raw.price ?? 0,
    stock,
    imageUrl,
    categoryIds: rawCategoryIds.length ? rawCategoryIds : [raw.categoryId ?? category.id].filter(Boolean),
    categoryNames: rawCategoryNames.length ? rawCategoryNames : [raw.categoryName ?? category.name].filter(Boolean),
    categoryId: raw.categoryId ?? rawCategoryIds[0] ?? category.id ?? null,
    categoryName: raw.categoryName ?? (rawCategoryNames.length ? rawCategoryNames.join(', ') : category.name) ?? '',
    status: statusCode === 1,
    statusCode,
    promotionName: raw.promotionName ?? '',
    promotionDiscountPercent: toNumber(raw.promotionDiscountPercent, 0),
    reviewCount: toNumber(raw.reviewCount, 0),
    ratingAverage: toNumber(raw.ratingAverage, 0),
    soldCount: toNumber(raw.soldCount, 0),
    createdAt: raw.createdAt ?? raw.created_at ?? raw.productDate ?? null,
    variants: normalizedVariants,
    images,
    minVariantPrice,
    maxVariantPrice
  }
}

export function normalizeBanner(raw = {}) {
  const configuredLink = String(raw.linkUrl || '')
  const productLink = raw.productName ? productPath({ name: raw.productName }) : '/shop'
  const safeLink = /^\/product\/\d+$/.test(configuredLink) ? productLink : (configuredLink || productLink)

  return {
    ...raw,
    id: raw.id ?? null,
    title: raw.title ?? raw.productName ?? `Banner ${raw.id ?? ''}`.trim(),
    subtitle: raw.subtitle ?? '',
    imageUrl: raw.imageUrl ?? raw.image ?? '',
    linkUrl: safeLink,
    buttonText: raw.buttonText ?? 'Xem ngay',
    sortOrder: raw.sortOrder ?? 0,
    isActive: raw.isActive ?? true,
    productId: raw.productId ?? null,
    dateBanner: raw.dateBanner ?? null,
    status: raw.status ?? true
  }
}
