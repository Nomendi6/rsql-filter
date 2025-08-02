import { Directive, Input } from '@angular/core';

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: '[cy]',
})
export class CyDirective {
  @Input() public cy?: string;
}
