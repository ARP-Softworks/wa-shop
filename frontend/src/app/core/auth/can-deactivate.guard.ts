import { CanDeactivateFn } from '@angular/router';
import { Observable } from 'rxjs';

export interface CanComponentDeactivate {
  canDeactivate: () => boolean | Observable<boolean>;
}

export const dirtyFormGuard: CanDeactivateFn<CanComponentDeactivate> = (component) => {
  if (component?.canDeactivate) {
    return component.canDeactivate();
  }
  return true;
};
