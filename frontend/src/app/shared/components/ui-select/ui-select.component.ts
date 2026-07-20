import { Component, ElementRef, HostListener, computed, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface UiSelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-ui-select',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UiSelectComponent),
      multi: true,
    },
  ],
  template: `
    <div class="combo" [class.is-open]="open()">
      <button
        type="button"
        class="ui-select combo__trigger"
        [id]="inputId()"
        [disabled]="disabled()"
        (click)="toggle()"
        (keydown)="onKeydown($event)"
        role="combobox"
        [attr.aria-expanded]="open()"
        aria-haspopup="listbox"
      >
        <span [class.combo__placeholder]="!selectedLabel()">{{ selectedLabel() || placeholder() }}</span>
      </button>
      @if (open() && options().length > 0) {
        <ul class="combo-panel" role="listbox">
          @for (option of options(); track option.value; let i = $index) {
            <li
              role="option"
              class="combo-option"
              [class.is-active]="i === highlight()"
              [attr.aria-selected]="option.value === value()"
              (mousedown)="select(option.value); $event.preventDefault()"
            >
              {{ option.label }}
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
})
export class UiSelectComponent implements ControlValueAccessor {
  readonly options = input<UiSelectOption[]>([]);
  readonly placeholder = input('Seleccionar');
  readonly inputId = input('');

  readonly value = signal('');
  readonly open = signal(false);
  readonly highlight = signal(0);
  readonly disabled = signal(false);

  readonly selectedLabel = computed(() => this.options().find((o) => o.value === this.value())?.label ?? '');

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor(private readonly host: ElementRef<HTMLElement>) {}

  writeValue(value: string | null): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }

  toggle(): void {
    if (this.disabled()) return;
    if (this.open()) {
      this.close();
    } else {
      this.highlight.set(Math.max(this.options().findIndex((o) => o.value === this.value()), 0));
      this.open.set(true);
    }
  }

  close(): void {
    if (this.open()) {
      this.open.set(false);
      this.onTouched();
    }
  }

  select(value: string): void {
    this.value.set(value);
    this.onChange(value);
    this.close();
  }

  onKeydown(event: KeyboardEvent): void {
    const opts = this.options();
    if (!this.open()) {
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp' || event.key === 'Enter') {
        this.toggle();
        event.preventDefault();
      }
      return;
    }
    if (event.key === 'ArrowDown') {
      this.highlight.set(Math.min(this.highlight() + 1, Math.max(opts.length - 1, 0)));
      event.preventDefault();
    } else if (event.key === 'ArrowUp') {
      this.highlight.set(Math.max(this.highlight() - 1, 0));
      event.preventDefault();
    } else if (event.key === 'Enter' && opts[this.highlight()]) {
      this.select(opts[this.highlight()].value);
      event.preventDefault();
    } else if (event.key === 'Escape') {
      this.close();
      event.preventDefault();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.host.nativeElement.contains(event.target as Node)) {
      this.close();
    }
  }
}
