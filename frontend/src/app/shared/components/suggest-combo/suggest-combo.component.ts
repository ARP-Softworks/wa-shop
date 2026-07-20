import { Component, ElementRef, HostListener, computed, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-suggest-combo',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SuggestComboComponent),
      multi: true,
    },
  ],
  template: `
    <div class="suggest-combo" [class.is-open]="open()">
      <input
        class="ui-select suggest-combo__input"
        type="text"
        [id]="inputId()"
        [placeholder]="placeholder()"
        [disabled]="disabled()"
        [value]="value()"
        (input)="onInput($event)"
        (focus)="openPanel()"
        (keydown)="onKeydown($event)"
        autocomplete="off"
        role="combobox"
        [attr.aria-expanded]="open()"
        aria-autocomplete="list"
      />
      @if (open() && filtered().length > 0) {
        <ul class="suggest-combo__list" role="listbox">
          @for (option of filtered(); track option; let i = $index) {
            <li
              role="option"
              class="suggest-combo__option"
              [class.is-active]="i === highlight()"
              (mousedown)="select(option); $event.preventDefault()"
            >
              {{ option }}
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
        position: relative;
      }

      .suggest-combo {
        position: relative;
      }

      .suggest-combo__input {
        width: 100%;
      }

      .suggest-combo__list {
        position: absolute;
        z-index: 40;
        top: calc(100% + 0.25rem);
        left: 0;
        right: 0;
        margin: 0;
        padding: 0.35rem;
        list-style: none;
        background: var(--color-surface, #fff);
        border: 1px solid var(--color-border, #e2e8f0);
        border-radius: var(--radius-sm, 0.5rem);
        box-shadow: 0 10px 30px rgb(15 23 42 / 10%);
        max-height: 14rem;
        overflow: auto;
      }

      .suggest-combo__option {
        padding: 0.55rem 0.7rem;
        border-radius: 0.4rem;
        cursor: pointer;
        color: var(--color-ink, #0f172a);
        font-size: 0.9375rem;
      }

      .suggest-combo__option:hover,
      .suggest-combo__option.is-active {
        background: var(--color-accent-soft, #eff6ff);
        color: var(--color-accent, #2563eb);
      }
    `,
  ],
})
export class SuggestComboComponent implements ControlValueAccessor {
  readonly options = input<string[]>([]);
  readonly placeholder = input('');
  readonly inputId = input('');

  readonly value = signal('');
  readonly open = signal(false);
  readonly highlight = signal(0);
  readonly disabled = signal(false);

  readonly filtered = computed(() => {
    const q = this.value().trim().toLowerCase();
    const opts = this.options();
    if (!q) {
      return opts.slice(0, 12);
    }
    return opts.filter((o) => o.toLowerCase().includes(q)).slice(0, 12);
  });

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

  onInput(event: Event): void {
    const next = (event.target as HTMLInputElement).value;
    this.value.set(next);
    this.onChange(next);
    this.open.set(true);
    this.highlight.set(0);
  }

  openPanel(): void {
    this.open.set(true);
  }

  select(option: string): void {
    this.value.set(option);
    this.onChange(option);
    this.open.set(false);
    this.onTouched();
  }

  onKeydown(event: KeyboardEvent): void {
    if (!this.open()) {
      if (event.key === 'ArrowDown') {
        this.open.set(true);
        event.preventDefault();
      }
      return;
    }
    const opts = this.filtered();
    if (event.key === 'ArrowDown') {
      this.highlight.set(Math.min(this.highlight() + 1, Math.max(opts.length - 1, 0)));
      event.preventDefault();
    } else if (event.key === 'ArrowUp') {
      this.highlight.set(Math.max(this.highlight() - 1, 0));
      event.preventDefault();
    } else if (event.key === 'Enter' && opts[this.highlight()]) {
      this.select(opts[this.highlight()]);
      event.preventDefault();
    } else if (event.key === 'Escape') {
      this.open.set(false);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.host.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
      this.onTouched();
    }
  }
}
