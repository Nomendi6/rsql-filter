import { FilterMetadata } from './filter-metadata.model';

/**
 * Represents metadata for filtering a data set.
 * Extends the PrimeNG FilterMetadata interface.
 */
export interface IExtendedFilter extends FilterMetadata {
  /**
   * Fields are defined for relationships.
   * For example, if the relationship is 'customer', the fields could be ['name', 'code'].
   */
  fields?: string[];
}
