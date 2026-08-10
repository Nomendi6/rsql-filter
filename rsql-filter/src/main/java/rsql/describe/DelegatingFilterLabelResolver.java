package rsql.describe;

import java.util.List;
import java.util.Objects;

/**
 * A resolver that forwards everything to another one, so a subclass overrides only what it changes.
 * <p>
 * {@link FilterLabelResolver} already gives every method a default, so a subclass could implement the interface
 * directly - but then the defaults win for everything it does not override, and a resolver passed in as a
 * collaborator is silently ignored. That matters most for value masking: a subclass that only translates field
 * names would, without this class, undo a delegate that replaces values with {@code ***}.
 * <p>
 * Forwarding is <em>pure</em>: every method goes to the delegate, including the ones whose interface default is
 * written in terms of another method. So a subclass that overrides {@link #valueLabel(Operand, Object)} does not
 * thereby change {@link #patternValueLabel} or {@link #parameterLabel} - those still ask the delegate. Override
 * them too if a pattern needle or a resolved parameter value should be formatted the same way.
 */
public class DelegatingFilterLabelResolver implements FilterLabelResolver {

    private final FilterLabelResolver delegate;

    /**
     * @param delegate Where every unhandled label comes from; {@link FilterLabelResolver#TECHNICAL} for the
     *                 built-in English defaults
     */
    public DelegatingFilterLabelResolver(FilterLabelResolver delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /** The resolver behind this one. */
    protected final FilterLabelResolver delegate() {
        return delegate;
    }

    @Override
    public String operandLabel(Operand operand) {
        return delegate.operandLabel(operand);
    }

    @Override
    public String operatorLabel(FilterCondition condition) {
        return delegate.operatorLabel(condition);
    }

    @Override
    public String valueLabel(Operand left, Object value) {
        return delegate.valueLabel(left, value);
    }

    @Override
    public String patternValueLabel(Operand left, Object raw, PatternShape shape, String needle) {
        return delegate.patternValueLabel(left, raw, shape, needle);
    }

    @Override
    public String parameterLabel(Operand left, String name, ParameterResolution resolution) {
        return delegate.parameterLabel(left, name, resolution);
    }

    @Override
    public String rightSideLabel(Operand left, RightSide rightSide) {
        return delegate.rightSideLabel(left, rightSide);
    }

    @Override
    public String junctionLabel(FilterGroup.Junction junction) {
        return delegate.junctionLabel(junction);
    }

    @Override
    public String joinList(List<String> renderedValues) {
        return delegate.joinList(renderedValues);
    }

    @Override
    public String joinRange(String from, String to) {
        return delegate.joinRange(from, to);
    }
}
