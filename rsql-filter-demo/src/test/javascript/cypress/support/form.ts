/* eslint-disable @typescript-eslint/no-namespace */
/// <reference types="cypress" />

Cypress.Commands.add('getFormControl', (controlName: string) => {
  return cy.get(`[data-cy="${controlName}"]`);
});

Cypress.Commands.add('getFormControlInParamForm', (controlName: string) => {
  return cy.get(`input[name="${controlName}Value"]`);
});

Cypress.Commands.add('getAccordionHeaderButton', (controlName: string) => {
  // PrimeNG has a specific id for the accordion header button in format: id="${controlName}_header_action"
  // Keep in mind that we add 'AccordionTab' to the controlName to get the correct id
  return cy.get(`[id="${controlName}AccordionTab_header_action"]`);
});

Cypress.Commands.add('getTabHeaderButton', (controlName: string) => {
  // Keep in mind that we add 'TabPanel' to the controlName to get the correct id
  return cy.get(`[id="${controlName}TabPanel"]`).parents('[id$="_header_action"]').first();
});

declare global {
  namespace Cypress {
    interface Chainable {
      getFormControl(controlName: string): Cypress.Chainable;
      getFormControlInParamForm(controlName: string): Cypress.Chainable;
      getAccordionHeaderButton(controlName: string): Cypress.Chainable;
      getTabHeaderButton(controlName: string): Cypress.Chainable;
    }
  }
}

// Convert this to a module instead of script (allows import/export)
export {};
