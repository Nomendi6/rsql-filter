export interface RequestOptions {
  page?: number;
  size?: number;
  sort?: string[];
  [key: string]: any; // For additional parameters
}
