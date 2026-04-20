import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'translateDay',
  standalone: true
})
export class TranslateDayPipe implements PipeTransform {
  transform(dayOfWeek: string): string {
    const days: Record<string, string> = {
      'MONDAY': 'Lundi',
      'TUESDAY': 'Mardi',
      'WEDNESDAY': 'Mercredi',
      'THURSDAY': 'Jeudi',
      'FRIDAY': 'Vendredi',
      'SATURDAY': 'Samedi',
      'SUNDAY': 'Dimanche'
    };
    return days[dayOfWeek] || dayOfWeek;
  }
}
