import { SafeHtml } from '@angular/platform-browser';

export interface IFormDocumentation {
  name: string;
  docsUrl: string;
  content: SafeHtml | string;
}
