package it.unive.lisa.tutorial;

import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.Satisfiability;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.ArithmeticOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
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
        if(this.isTop()) return new StringRepresentation("T");
        if(this.isBottom()) return new StringRepresentation("_|_");
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
            SemanticOracle oracle)
            throws SemanticException
    {
        Object value = constant.getValue();
        if (value instanceof Float) {
            return new FloatConstantSetDomain((Float) value);
        }
        return BaseNonRelationalValueDomain.super.evalNonNullConstant(constant, pp, oracle);
    }
  
    @Override
    public FloatConstantSetDomain evalUnaryExpression(
            UnaryOperator operator,
            FloatConstantSetDomain arg,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException
    {
        if (operator instanceof NumericNegation) {
            if (arg.isTop()) return TOP;
            if (arg.isBottom()) return BOTTOM;

            Set<Float> negatedValues = new HashSet<>();
            for (Float v : arg.values) {
                negatedValues.add(-v);
            }
            return new FloatConstantSetDomain(negatedValues);
        }
        return arg.top();
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
        if (operator instanceof ArithmeticOperator) {
            if (left.isBottom() || right.isBottom()) return BOTTOM;
            if (left.isTop() || right.isTop()) return TOP;

            HashSet<Float> results = new HashSet<>();
            for (Float l : left.values) {
                for (Float r : right.values) {
                    float result;
                    if (operator instanceof AdditionOperator)
                        result = l + r;
                    else if (operator instanceof SubtractionOperator)
                        result = l - r;
                    else if (operator instanceof MultiplicationOperator)
                        result = l * r;
                    else if (operator instanceof DivisionOperator)
                        result = l / r;
                    else
                        return TOP;

                    if (Float.isNaN(result)) return TOP;
                    results.add(result);
                }
            }

            if (results.size() > N) return TOP;
            return new FloatConstantSetDomain(results);
        }
        return BaseNonRelationalValueDomain.super.evalBinaryExpression(operator, left, right, pp, oracle);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(
            BinaryOperator operator,
            FloatConstantSetDomain left,
            FloatConstantSetDomain right,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException
    {
        if (left.isTop() || right.isTop())
            return Satisfiability.UNKNOWN;
        if (left.isBottom() || right.isBottom())
            return Satisfiability.BOTTOM;

        if (operator instanceof ComparisonLt) {
            boolean allTrue = true;
            boolean allFalse = true;
            for (Float l : left.values)
                for (Float r : right.values) {
                    if (l < r) allFalse = false;
                    else allTrue = false;
                }
            if (allTrue) return Satisfiability.SATISFIED;
            if (allFalse) return Satisfiability.NOT_SATISFIED;
            return Satisfiability.UNKNOWN;
        }

        return BaseNonRelationalValueDomain.super.satisfiesBinaryExpression(operator, left, right, pp, oracle);
    }

    @Override
    public ValueEnvironment<FloatConstantSetDomain> assumeBinaryExpression(
            ValueEnvironment<FloatConstantSetDomain> environment,
            BinaryOperator operator,
            ValueExpression left,
            ValueExpression right,
            ProgramPoint src,
            ProgramPoint dest,
            SemanticOracle oracle)
            throws SemanticException
    {
        if (operator instanceof ComparisonLt && left instanceof Identifier && right instanceof Constant) {
            Identifier x = (Identifier) left;
            Constant c = (Constant) right;
            if (c.getValue() instanceof Float) {
                FloatConstantSetDomain xVals = environment.getState(x);
                if (!xVals.isTop() && !xVals.isBottom()) {
                    Set<Float> filtered = new HashSet<>();
                    for (Float v : xVals.values)
                        if (v < (Float) c.getValue()) filtered.add(v);
                    if (filtered.isEmpty())
                        return environment.bottom();
                    environment = environment.putState(x, new FloatConstantSetDomain(filtered));
                }
            }
        }

        if (operator instanceof ComparisonLt && left instanceof Constant && right instanceof Identifier) {
            Constant c = (Constant) left;
            Identifier x = (Identifier) right;
            if (c.getValue() instanceof Float) {
                FloatConstantSetDomain xVals = environment.getState(x);
                if (!xVals.isTop() && !xVals.isBottom()) {
                    Set<Float> filtered = new HashSet<>();
                    for (Float v : xVals.values)
                        if ((Float) c.getValue() < v) filtered.add(v);
                    if (filtered.isEmpty())
                        return environment.bottom();
                    environment = environment.putState(x, new FloatConstantSetDomain(filtered));
                }
            }
        }

        if (operator instanceof ComparisonLt && left instanceof Identifier && right instanceof Identifier) {
            Identifier x = (Identifier) left;
            Identifier y = (Identifier) right;
            FloatConstantSetDomain xVals = environment.getState(x);
            FloatConstantSetDomain yVals = environment.getState(y);
            if (!xVals.isTop() && !xVals.isBottom() && !yVals.isTop() && !yVals.isBottom()) {
                Set<Float> xFiltered = new HashSet<>();
                Set<Float> yFiltered = new HashSet<>();
                for (Float xv : xVals.values)
                    for (Float yv : yVals.values)
                        if (xv < yv) {
                            xFiltered.add(xv);
                            yFiltered.add(yv);
                        }
                if (xFiltered.isEmpty() || yFiltered.isEmpty())
                    return environment.bottom();
                environment = environment.putState(x, new FloatConstantSetDomain(xFiltered));
                environment = environment.putState(y, new FloatConstantSetDomain(yFiltered));
            }
        }
        return environment;
    }
}
