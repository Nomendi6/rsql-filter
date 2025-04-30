import { IScenarioStepActionConfig } from 'models/scenarios/scenario-step-action-config.model';

export interface ITestSuite {
  metadata: {
    formName: string;
  };
  methods: {
    [key: string]: (config?: IScenarioStepActionConfig) => Mocha.Test;
  };
}
