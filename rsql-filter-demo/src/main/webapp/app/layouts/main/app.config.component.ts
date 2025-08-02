import { Component, OnInit } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { MainComponent } from './main.component';

@Component({
  selector: 'app-config',
  template: `
    <div class="layout-config" [ngClass]="{ 'layout-config-active': app.configActive }" (click)="app.onConfigClick()">
      <a style="cursor: pointer" id="layout-config-button" class="layout-config-button" (click)="onConfigButtonClick($event)">
        <em class="pi pi-cog"></em>
      </a>
      <a style="cursor: pointer" class="layout-config-close" (click)="onConfigCloseClick($event)">
        <em class="pi pi-times"></em>
      </a>
      <div class="layout-config-content">
        <h5>Menu Type</h5>
        <div class="grid grid-cols-12 gap-4">
          <div class="col-span-6">
            <div class="p-field-radiobutton">
              <p-radioButton inputId="overlay" name="layoutMode" [(ngModel)]="app.horizontalMenu"></p-radioButton>
              <label for="static">Overlay</label>
            </div>
          </div>
          <div class="col-span-6">
            <div class="p-field-radiobutton">
              <p-radioButton inputId="horizontal" name="layoutMode" value="true" [(ngModel)]="app.horizontalMenu"></p-radioButton>
              <label for="horizontal">Horizontal</label>
            </div>
          </div>
        </div>

        <h5>Color Scheme</h5>
        <div class="grid grid-cols-12 gap-4">
          <div class="col-span-6">
            <div class="p-field-radiobutton">
              <p-radioButton
                inputId="light"
                name="colorScheme"
                value="false"
                [(ngModel)]="app.darkMode"
                (onClick)="changeLayout($event, false)"
              ></p-radioButton>
              <label for="light">Light</label>
            </div>
          </div>
          <div class="col-span-6">
            <div class="p-field-radiobutton">
              <p-radioButton
                inputId="dark"
                name="colorScheme"
                value="true"
                [(ngModel)]="app.darkMode"
                (onClick)="changeLayout($event, true)"
              ></p-radioButton>
              <label for="dark">Dark</label>
            </div>
          </div>
        </div>

        <h5>Input Style</h5>
        <div class="p-formgroup-inline">
          <div class="p-field-radiobutton">
            <p-radioButton inputId="input_outlined" name="inputstyle" [(ngModel)]="app.inputStyle" value="outlined"></p-radioButton>
            <label for="input_outlined">Outlined</label>
          </div>
          <div class="p-field-radiobutton">
            <p-radioButton inputId="input_filled" name="inputstyle" [(ngModel)]="app.inputStyle" value="filled"></p-radioButton>
            <label for="input_filled">Filled</label>
          </div>
        </div>

        <h5>Ripple Effect</h5>
        <p-toggleswitch [ngModel]="app.ripple" (onChange)="app.onRippleChange($event)"></p-toggleswitch>

        <h5>Menu Colors</h5>
        <div class="p-formgroup-inline">
          <div class="p-field-radiobutton">
            <p-radioButton
              inputId="lightMenu"
              name="menuColor"
              value="light"
              [ngModel]="app.menuColorMode"
              (onClick)="changeMenuColorMode($event, 'light')"
            ></p-radioButton>
            <label for="lightMenu">Light</label>
          </div>
          <div class="p-field-radiobutton">
            <p-radioButton
              inputId="darkMenu"
              name="menuColor"
              value="dark"
              [ngModel]="app.menuColorMode"
              (onClick)="changeMenuColorMode($event, 'dark')"
            ></p-radioButton>
            <label for="darkMenu">Dark</label>
          </div>
          <div class="p-field-radiobutton">
            <p-radioButton
              inputId="customMenu"
              name="menuColor"
              value="custom"
              [ngModel]="app.menuColorMode"
              (onClick)="changeMenuColorMode($event, 'custom')"
            ></p-radioButton>
            <label for="customMenu">Custom</label>
          </div>
        </div>
        <div class="layout-themes">
          <div *ngFor="let color of menuColors">
            <a style="cursor: pointer" (click)="changeMenuColor($event, color.file)" [ngStyle]="{ 'background-color': color.color }">
              <img
                src="content/layout/images/configurator/menu/theme/{{ color.image }}"
                *ngIf="app.menuColorMode === 'custom'"
                style="height: 100%; width: 100%;"
                alt="{{ color.name }}"
              />
              <em class="pi pi-check" *ngIf="app.layoutColor === color.file"></em>
            </a>
          </div>
        </div>

        <h5>Themes</h5>
        <div class="layout-themes">
          <div *ngFor="let t of componentThemes">
            <a style="cursor: pointer" (click)="changeComponentTheme($event, t.file)" [ngStyle]="{ 'background-color': t.color }">
              <em class="pi pi-check" *ngIf="app.themeColor === t.file"></em>
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
  animations: [
    trigger('children', [
      state(
        'hiddenAnimated',
        style({
          opacity: 0,
          transform: ' translateX(-50%) translateY(-50%)',
        }),
      ),
      state(
        'visibleAnimated',
        style({
          opacity: 1,
          transform: 'translateX(-50%) translateY(-50%) scale(1)',
        }),
      ),
      transition('visibleAnimated => hiddenAnimated', animate('150ms cubic-bezier(0, 0, 0.2, 1)')),
      transition('hiddenAnimated => visibleAnimated', animate('150ms cubic-bezier(0, 0, 0.2, 1)')),
    ]),
  ],
})
export class AppConfigComponent implements OnInit {
  darkColors: any;

  lightColors: any;

  customColors: any;

  menuColors: any;

  componentThemes: any;

  constructor(public app: MainComponent) {}

  ngOnInit(): void {
    this.lightColors = [
      { name: 'Blue', file: 'blue', color: '#5e81ac' },
      { name: 'Green', file: 'green', color: '#A3BE8C' },
      { name: 'Yellow', file: 'yellow', color: '#EBCB8B' },
      { name: 'Cyan', file: 'cyan', color: '#88C0D0' },
      { name: 'Purple', file: 'purple', color: '#B48EAD' },
      { name: 'Orange', file: 'orange', color: '#D08770' },
      { name: 'Teal', file: 'teal', color: '#88D0BD' },
      { name: 'Magenta', file: 'magenta', color: '#BD69AE' },
      { name: 'Lime', file: 'lime', color: '#B9BE7F' },
      { name: 'Brown', file: 'brown', color: '#BE9B7F' },
      { name: 'Red', file: 'red', color: '#F28686' },
      { name: 'Indigo', file: 'indigo', color: '#8886F2' },
    ];

    this.darkColors = [
      { name: 'Blue', file: 'blue', color: '#5e81ac' },
      { name: 'Green', file: 'green', color: '#A3BE8C' },
      { name: 'Yellow', file: 'yellow', color: '#EBCB8B' },
      { name: 'Cyan', file: 'cyan', color: '#88C0D0' },
      { name: 'Purple', file: 'purple', color: '#B48EAD' },
      { name: 'Orange', file: 'orange', color: '#D08770' },
      { name: 'Teal', file: 'teal', color: '#88D0BD' },
      { name: 'Magenta', file: 'magenta', color: '#BD69AE' },
      { name: 'Lime', file: 'lime', color: '#B9BE7F' },
      { name: 'Brown', file: 'brown', color: '#BE9B7F' },
      { name: 'Red', file: 'red', color: '#F28686' },
      { name: 'Indigo', file: 'indigo', color: '#8886F2' },
    ];

    this.customColors = [
      { name: 'Chile', file: 'chile', image: 'chile.png' },
      { name: 'Naples', file: 'naples', image: 'naples.png' },
      { name: 'Georgia', file: 'georgia', image: 'georgia.png' },
      { name: 'Infinity', file: 'infinity', image: 'infinity.png' },
      { name: 'Chicago', file: 'chicago', image: 'chicago.png' },
      { name: 'Majesty', file: 'majesty', image: 'majesty.png' },
      { name: 'Fish', file: 'fish', image: 'fish.png' },
      { name: 'Dawn', file: 'dawn', image: 'dawn.png' },
      { name: 'Record', file: 'record', image: 'record.png' },
      { name: 'Pool', file: 'pool', image: 'pool.png' },
      { name: 'Metal', file: 'metal', image: 'metal.png' },
      { name: 'China', file: 'china', image: 'china.png' },
      { name: 'Table', file: 'table', image: 'table.png' },
      { name: 'Panorama', file: 'panorama', image: 'panorama.png' },
      { name: 'Barcelona', file: 'barcelona', image: 'barcelona.png' },
      { name: 'Underwater', file: 'underwater', image: 'underwater.png' },
      { name: 'Symmetry', file: 'symmetry', image: 'symmetry.png' },
      { name: 'Rain', file: 'rain', image: 'rain.png' },
      { name: 'Utah', file: 'utah', image: 'utah.png' },
      { name: 'Wave', file: 'wave', image: 'wave.png' },
      { name: 'Flora', file: 'flora', image: 'flora.png' },
      { name: 'Speed', file: 'speed', image: 'speed.png' },
      { name: 'Canopy', file: 'canopy', image: 'canopy.png' },
      { name: 'SanPaolo', file: 'sanpaolo', image: 'sanpaolo.png' },
      { name: 'Basketball', file: 'basketball', image: 'basketball.png' },
      { name: 'Misty', file: 'misty', image: 'misty.png' },
      { name: 'Summit', file: 'summit', image: 'summit.png' },
      { name: 'Wall', file: 'wall', image: 'wall.png' },
      { name: 'Ferris', file: 'ferris', image: 'ferris.png' },
      { name: 'Ship', file: 'ship', image: 'ship.png' },
      { name: 'NY', file: 'ny', image: 'ny.png' },
      { name: 'Cyan', file: 'cyan', image: 'cyan.png' },
      { name: 'Violet', file: 'violet', image: 'violet.png' },
      { name: 'Red', file: 'red', image: 'red.png' },
      { name: 'Blue', file: 'blue', image: 'blue.png' },
      { name: 'Porsuk', file: 'porsuk', image: 'porsuk.png' },
      { name: 'Pink', file: 'pink', image: 'pink.png' },
      { name: 'Purple', file: 'purple', image: 'purple.png' },
      { name: 'Orange', file: 'orange', image: 'orange.png' },
    ];

    this.menuColors = this.lightColors;

    this.componentThemes = [
      { name: 'Blue', file: 'blue', color: '#5E81AC' },
      { name: 'Green', file: 'green', color: '#99CE6B' },
      { name: 'Yellow', file: 'yellow', color: '#EBCB8B' },
      { name: 'Cyan', file: 'cyan', color: '#88C0D0' },
      { name: 'Purple', file: 'purple', color: '#B48EAD' },
      { name: 'Orange', file: 'orange', color: '#D08770' },
      { name: 'Teal', file: 'teal', color: '#88D0BD' },
      { name: 'Magenta', file: 'magenta', color: '#BD69AE' },
      { name: 'Lime', file: 'lime', color: '#B9BE7F' },
      { name: 'Brown', file: 'brown', color: '#BE9B7F' },
      { name: 'Red', file: 'red', color: '#F28686' },
      { name: 'Indigo', file: 'indigo', color: '#8886F2' },
    ];
  }

  changeLayout(event: any, mode: any): void {
    this.app.darkMode = mode;
    if (mode === true) {
      this.app.menuColorMode = 'dark';
      this.app.menuColor = 'layout-menu-dark';
      this.app.layoutColor = this.darkColors[0].file;
      this.menuColors = this.darkColors;
      this.changeLightDarkLayout('layout-css', this.darkColors[0].file, 'layout-dark');
      this.changeLightDarkTheme('theme-css', 'theme-dark');
    } else {
      this.app.menuColorMode = 'light';
      this.app.menuColor = 'layout-menu-light';
      this.app.layoutColor = this.lightColors[0].file;
      this.menuColors = this.lightColors;
      this.changeLightDarkLayout('layout-css', this.lightColors[0].file, 'layout-light');
      this.changeLightDarkTheme('theme-css', 'theme-light');
    }

    event.preventDefault();
  }

  changeLightDarkTheme(id: any, value: string): void {
    const element = document.getElementById(id);
    const urlTokens = element?.getAttribute('href')?.split('/');
    urlTokens![urlTokens!.length - 1] = `${value}.css`;
    const newURL = urlTokens!.join('/');

    this.replaceLink(element, newURL);
  }

  changeLightDarkLayout(id: any, color: any, mode: string): void {
    const element = document.getElementById(id);
    const urlTokens = element!.getAttribute('href')!.split('/');
    urlTokens[urlTokens.length - 2] = color;
    urlTokens[urlTokens.length - 1] = `${mode}.css`;
    const newURL = urlTokens.join('/');

    this.replaceLink(element, newURL);
  }

  changeMenuColorMode(event: any, mode: string): void {
    this.app.menuColorMode = mode;
    if (mode !== 'custom') {
      if (mode === 'light') {
        this.menuColors = this.lightColors;
        this.changeMenuColor(event, this.lightColors[0].file);
      } else {
        this.menuColors = this.darkColors;
        this.changeMenuColor(event, this.darkColors[0].file);
      }
    } else {
      this.menuColors = this.customColors;
      this.changeMenuColor(event, this.customColors[0].file);
    }
  }

  changeMenuColor(event: any, color: string): void {
    if (this.app.menuColorMode !== 'custom') {
      this.app.menuColor = `layout-menu-${this.app.menuColorMode}`;
      if (this.app.menuColorMode === 'dark') {
        this.app.layoutColor = color;
        this.changeStyleSheetsColor('layout-css', color);
      } else {
        this.app.layoutColor = color;
        this.changeStyleSheetsColor('layout-css', color);
      }
    } else {
      this.app.layoutColor = color;
      this.app.menuColor = `layout-menu-${color}`;
    }

    event.preventDefault();
  }

  changeComponentTheme(event: any, color: any): void {
    this.app.themeColor = color;
    this.changeStyleSheetsColor('theme-css', color);

    event.preventDefault();
  }

  changeStyleSheetsColor(id: any, value: string): void {
    const element = document.getElementById(id);
    const urlTokens = element!.getAttribute('href')!.split('/');
    urlTokens[urlTokens.length - 2] = value;
    const newURL = urlTokens.join('/');

    this.replaceLink(element, newURL);
  }

  onConfigButtonClick(event: any): void {
    this.app.configActive = !this.app.configActive;
    event.preventDefault();
  }

  onConfigCloseClick(event: any): void {
    this.app.configActive = false;
    event.preventDefault();
  }

  replaceLink(linkElement: any, href: any): void {
    if (this.isIE()) {
      linkElement.setAttribute('href', href);
    } else {
      const id: string = linkElement.getAttribute('id');
      const cloneLinkElement = linkElement.cloneNode(true);

      cloneLinkElement.setAttribute('href', href);
      cloneLinkElement.setAttribute('id', `${id}-clone`);

      linkElement.parentNode.insertBefore(cloneLinkElement, linkElement.nextSibling);

      cloneLinkElement.addEventListener('load', () => {
        linkElement.remove();
        cloneLinkElement.setAttribute('id', id);
      });
    }
  }

  isIE(): any {
    return /(MSIE|Trident\/|Edge\/)/i.test(window.navigator.userAgent);
  }
}
