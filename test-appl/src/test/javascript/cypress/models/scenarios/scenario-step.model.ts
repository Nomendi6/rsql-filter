import { IScenarioStepAction } from './scenario-step-action.model';

export interface IScenarioStep {
  formName: string;
  actionsToPerform: IScenarioStepAction[];
}
