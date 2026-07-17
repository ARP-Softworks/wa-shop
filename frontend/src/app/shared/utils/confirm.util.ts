/** Simple confirmation helper for destructive admin actions. */
export function confirmAction(message: string): boolean {
  return globalThis.confirm(message);
}
