export function scrollToTop(behavior: ScrollBehavior = 'smooth'): void {
  if (typeof window !== 'undefined') {
    window.scrollTo({ top: 0, behavior });
  }
}
