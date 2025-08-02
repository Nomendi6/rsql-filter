import { IScenarioStepActionConfig } from 'models/scenarios/scenario-step-action-config.model';
import { ITestSuite } from 'models/test-suite/test-suite.model';

export const commonTestActions: ITestSuite = {
  metadata: {
    formName: 'Common',
  },
  methods: {
    navigateToUrl(stepConfig?: IScenarioStepActionConfig) {
      if (!stepConfig?.data.url) {
        throw new Error('The url is required for the navigateToUrl action');
      }

      return it(`should navigate to the url: ${stepConfig.data.url.value}`, () => {
        // WHEN
        cy.visit(stepConfig.data.url.value);

        // THEN
        cy.url().should('match', new RegExp(stepConfig.data.url.value));
      });
    },
  },
};
