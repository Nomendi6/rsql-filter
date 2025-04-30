import { IScenarioStepActionConfigField } from './scenario-step-action-config-field.model';

export interface IScenarioStepActionConfig {
  data: {
    [key: string]: IScenarioStepActionConfigField;
  };
}
