import { expect } from '@jest/globals';
import { CyDirective } from './cy.directive';

describe('CyDirective', () => {
  it('should create an instance', () => {
    const directive = new CyDirective();
    expect(directive).toBeTruthy();
  });
});
