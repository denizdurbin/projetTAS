package it.unive.lisa.tutorial;

import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.HashSet;
import java.util.Objects;


public class FloatConstantSetDomain implements BaseNonRelationalValueDomain<FloatConstantSetDomain> {

    public static final int N = 3; //max number of values in the set
    public static final FloatConstantSetDomain TOP = new FloatConstantSetDomain((Set<Float>)null);
    public static final FloatConstantSetDomain BOTTOM = new FloatConstantSetDomain(Set.of());

    private final Set<Float> values;

    public FloatConstantSetDomain(Set<Float> values) {
        if(values != null && values.size() > N) {
            this.values = null; // if bigger than n, TOP
        } else if(values == null) {
            this.values = null; // TOP
        } else {
            this.values = Set.copyOf(values);
        }
    }
    
    public FloatConstantSetDomain(Float value) {
        if(value == null) {
            this.values = null; // TOP
        } else {
            this.values = Set.of(value);
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

        if (intersection.isEmpty()) return BOTTOM;
        
        return new FloatConstantSetDomain(intersection);
    }

    @Override
    public boolean lessOrEqualAux(FloatConstantSetDomain other) {
        if(this.isTop()) return other.isTop();
        if(other.isTop()) return true;
        if(this.isBottom()) return true;
        if(other.isBottom()) return false;

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
        if(this.isTop()) return new StringRepresentation("TOP");
        if(this.isBottom()) return new StringRepresentation("BOTTOM");
        return new StringRepresentation(values.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FloatConstantSetDomain other)) return false;
        return Objects.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    // logic for evaluating expressions below

    //TODO
    @Override
    public FloatConstantSetDomain evalNonNullConstant(
        Constant constant,
        ProgramPoint pp,
        SemanticOracle oracle
    ) throws SemanticException
    {
        return this;
    }
  
    @Override
    public FloatConstantSetDomain evalUnaryExpression(
        UnaryOperator operator,
        FloatConstantSetDomain arg,
        ProgramPoint pp,
        SemanticOracle oracle)
        throws SemanticException
    {
        return this;
    }

    @Override
    public FloatConstantSetDomain evalBinaryExpression(
        BinaryOperator operator,
        FloatConstantSetDomain left,
        FloatConstantSetDomain right,
        ProgramPoint pp,
        SemanticOracle oracle)
        throws SemanticException
        { 
            return this;
        }
    
}
