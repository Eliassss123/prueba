import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthViewModel } from '../viewmodels/auth.viewmodel';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthViewModel);
  const router = inject(Router);
  if (auth.session()) {
    return true;
  }
  return router.createUrlTree(['/']);
};
