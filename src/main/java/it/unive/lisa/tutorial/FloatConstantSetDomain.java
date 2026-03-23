package it.unive.lisa.tutorial;

import java.util.Set;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalTypeDomain;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.HashSet;


public class FloatConstantSetDomain implements BaseNonRelationalTypeDomain<FloatConstantSetDomain> {

    public static final int N = 3; //max number of values in the set
    public static final FloatConstantSetDomain TOP = new FloatConstantSetDomain(null);
    public static final FloatConstantSetDomain BOTTOM = new FloatConstantSetDomain(Set.of());

    private final Set<Float> values;

    public FloatConstantSetDomain(Set<Float> values) {
        if(values != null && values.size() > N) {
            this.values = null; // if bigger than n, TOP
        } else {
            this.values = Set.copyOf(values);
        }
    }

    @Override
    public FloatConstantSetDomain lubAux(FloatConstantSetDomain other) {
        if (this.isTop() || other.isTop()) return TOP;
        if (this.isBottom()) return other;
        if (other.isBottom()) return this;

        Set<Float> union = new HashSet<>(this.values);
        union.addAll(other.values);

        if (union.size() > N) return TOP;
        return new FloatConstantSetDomain(union);
    }

    @Override
    public FloatConstantSetDomain glbAux(FloatConstantSetDomain other) {
        if (this.isBottom() || other.isBottom()) return BOTTOM;
        if (this.isTop()) return other;
        if (other.isTop()) return this;

        Set<Float> intersection = new HashSet<>(this.values);
        intersection.retainAll(other.values);

        return new FloatConstantSetDomain(intersection);
    }

    @Override
    public boolean lessOrEqualAux(FloatConstantSetDomain other) {
        if (this.isBottom() || other.isTop()) return true;
        if (this.isTop() || other.isBottom()) return false;
        return other.values.containsAll(this.values);
    }

    @Override
    public FloatConstantSetDomain top() {
        return TOP;
    }

    @Override
    public FloatConstantSetDomain bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return values == null;
    }

    @Override
    public boolean isBottom() {
        return values != null && values.isEmpty();  
    }

    @Override
    public StructuredRepresentation representation() {
        return null;
    }

    @Override
    public Set<Type> getRuntimeTypes() {
        throw new UnsupportedOperationException("Unimplemented method 'getRuntimeTypes'");
    }

    
    
}
