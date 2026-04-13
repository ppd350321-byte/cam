import { flattenPermissionCodes } from '@/constants/permissions';

export function isAdmin(user) {
  return Boolean(user && (user.isAdmin || user.username === 'admin'));
}

export function hasPermission(user, permissionCode) {
  if (!permissionCode) return true;
  if (isAdmin(user)) return true;
  const permissions = getUserPermissions(user);
  return permissions.includes(permissionCode);
}

export function hasAnyPermission(user, permissionCodes) {
  if (!Array.isArray(permissionCodes) || permissionCodes.length === 0) return true;
  return permissionCodes.some((code) => hasPermission(user, code));
}

export function getAllPermissionCodes() {
  return flattenPermissionCodes();
}

export function getUserPermissions(user) {
  return Array.isArray(user && user.permissions) ? user.permissions : [];
}
