import { IScenarioStep } from './scenario-step.model';

export interface ITestScenario {
  name: string;
  sequence: number;
  relatedForm: string;
  steps: IScenarioStep[];
}
