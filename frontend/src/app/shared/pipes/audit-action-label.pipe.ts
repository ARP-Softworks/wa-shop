import { Pipe, PipeTransform } from '@angular/core';
import { AuditAction } from '../models/admin.models';

@Pipe({ name: 'auditActionLabel', standalone: true })
export class AuditActionLabelPipe implements PipeTransform {
  transform(value: AuditAction | null | undefined): string {
    switch (value) {
      case 'CREATE':
        return 'Creación';
      case 'UPDATE':
        return 'Actualización';
      case 'DELETE':
        return 'Eliminación';
      case 'PUBLISH':
        return 'Publicación';
      case 'UNPUBLISH':
        return 'Despublicación';
      case 'LOGIN':
        return 'Inicio de sesión';
      case 'OTHER':
        return 'Otro';
      default:
        return '';
    }
  }
}
