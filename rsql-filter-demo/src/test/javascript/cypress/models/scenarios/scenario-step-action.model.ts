import { IScenarioStepActionConfig } from './scenario-step-action-config.model';

export interface IScenarioStepAction {
  name: string;
  type: string; // TODO: Create a type for this
  supportedTemplates: string[];
  parameters: IScenarioStepActionParameter[];
  config?: IScenarioStepActionConfig;
}

export interface IScenarioStepActionParameter {
  name: string;
  type: ScenarioStepActionParameterType;
}

type ScenarioStepActionParameterType = 'selectSubform' | 'inputString';
