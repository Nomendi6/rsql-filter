/* eslint-disable @typescript-eslint/no-namespace */

import {
  accountMenuSelector,
  adminMenuSelector,
  entityItemSelector,
  loginItemSelector,
  logoutItemSelector,
  navbarSelector,
  passwordItemSelector,
  registerItemSelector,
  settingsItemSelector,
} from './commands';

Cypress.Commands.add('clickOnLoginItem', () => {
  return cy.get(navbarSelector).get(accountMenuSelector).get(loginItemSelector).click({ force: true });
});

Cypress.Commands.add('clickOnLogoutItem', () => {
  return cy.get(navbarSelector).get(accountMenuSelector).get(logoutItemSelector).click({ force: true });
});

Cypress.Commands.add('clickOnRegisterItem', () => {
  return cy.get(navbarSelector).get(accountMenuSelector).get(registerItemSelector).click({ force: true });
});

Cypress.Commands.add('clickOnSettingsItem', () => {
  return cy.get(navbarSelector).get(accountMenuSelector).get(settingsItemSelector).click({ force: true });
});

Cypress.Commands.add('clickOnPasswordItem', () => {
  return cy.get(navbarSelector).get(accountMenuSelector).get(passwordItemSelector).click({ force: true });
});

Cypress.Commands.add('clickOnAdminMenuItem', (item: string) => {
  return cy.get(navbarSelector).get(adminMenuSelector).get(`[data-automationid="admin/${item}"]`).click({ force: true });
});

Cypress.Commands.add('clickOnEntityMenuItem', (entityName: string) => {
  return cy.get(navbarSelector).get(entityItemSelector).get(`.dropdown-item[href="/${entityName}"]`).click({ force: true });
});

Cypress.Commands.add('clickOnFormMenuItem', (formName: string) => {
  cy.get(navbarSelector).click({ force: true });
  return cy.get(`[data-automationid="${formName}"]`).click({ force: true });
});

Cypress.Commands.add('clickOnFormSubMenuItem', (menuName: string, formName: string) => {
  cy.get(navbarSelector).get(`[data-automationid="${menuName}"]`).click({ force: true });
  return cy.get(`[data-automationid="${formName}"]`).click({ force: true });
});

declare global {
  namespace Cypress {
    interface Chainable {
      clickOnLoginItem(): Cypress.Chainable;
      clickOnLogoutItem(): Cypress.Chainable;
      clickOnRegisterItem(): Cypress.Chainable;
      clickOnSettingsItem(): Cypress.Chainable;
      clickOnPasswordItem(): Cypress.Chainable;
      clickOnAdminMenuItem(item: string): Cypress.Chainable;
      clickOnEntityMenuItem(entityName: string): Cypress.Chainable;
      clickOnFormMenuItem(formName: string): Cypress.Chainable;
      clickOnFormSubMenuItem(menuName: string, formName: string): Cypress.Chainable;
    }
  }
}

// Convert this to a module instead of script (allows import/export)
export {};
