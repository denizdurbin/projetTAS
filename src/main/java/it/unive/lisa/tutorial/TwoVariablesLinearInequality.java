package it.unive.lisa.tutorial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.Satisfiability;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class TwoVariablesLinearInequality implements ValueDomain<TwoVariablesLinearInequality> {

    private static final TwoVariablesLinearInequality TOP = new TwoVariablesLinearInequality(false, Set.of());
    private static final TwoVariablesLinearInequality BOTTOM = new TwoVariablesLinearInequality(true, Set.of());

    private final boolean isBottom;
    private final Set<Inequality> inequalities;


    public TwoVariablesLinearInequality() {
        this(false, Set.of()); // TOP
    }

    public TwoVariablesLinearInequality(Set<Inequality> inequalities) {
        this(false, inequalities);
    }

    private TwoVariablesLinearInequality(boolean isBottom, Set<Inequality> inequalities) {
        this.isBottom = isBottom;
        this.inequalities = inequalities == null ? Set.of() : Set.copyOf(inequalities);
    }

    @Override
    public TwoVariablesLinearInequality assign(Identifier id, ValueExpression expression, ProgramPoint pp,
        SemanticOracle oracle) throws SemanticException {
    if (isBottom()) return this;

    if (expression instanceof Identifier && ((Identifier) expression).equals(id)) {
        return this;
    }

    if (expression instanceof BinaryExpression) {
        BinaryExpression bin = (BinaryExpression) expression;
        SymbolicExpression l = bin.getLeft();
        SymbolicExpression r = bin.getRight();

        if (bin.getOperator() instanceof AdditionOperator) {
            Double delta = selfShiftDelta(id, l, r);
            if (delta != null) return shift(id, delta);
        } else if (bin.getOperator() instanceof SubtractionOperator) {
            if (l instanceof Identifier && ((Identifier) l).equals(id) && r instanceof Constant) {
                Double val = toDouble(((Constant) r).getValue());
                if (val != null) return shift(id, -val);
            } else if (l instanceof Constant && r instanceof Identifier && ((Identifier) r).equals(id)) {
                Double val = toDouble(((Constant) l).getValue());
                if (val != null) return reflect(id, val);
            }
        }
    }

    Set<Inequality> updated = new HashSet<>();
    for (Inequality ineq : inequalities) {
        if (!ineq.involves(id)) updated.add(ineq);
    }

    if (expression instanceof Constant) {
        Double val = toDouble(((Constant) expression).getValue());
        if (val != null) {
            updated.add(new Inequality(1, id, 0, null, val));
            updated.add(new Inequality(-1, id, 0, null, -val));
        }
    } else if (expression instanceof Identifier) {
        Identifier y = (Identifier) expression;
        updated.add(new Inequality(1, id, -1, y, 0));
        updated.add(new Inequality(-1, id, 1, y, 0));
    } else if (expression instanceof BinaryExpression) {
        BinaryExpression bin = (BinaryExpression) expression;

        if (bin.getOperator() instanceof AdditionOperator) {
            if (bin.getLeft() instanceof Identifier && bin.getRight() instanceof Constant) {
                Identifier y = (Identifier) bin.getLeft();
                Double val = toDouble(((Constant) bin.getRight()).getValue());
                if (val != null) {
                    updated.add(new Inequality(1, id, -1, y, val));
                    updated.add(new Inequality(-1, id, 1, y, -val));
                }
            } else if (bin.getLeft() instanceof Constant && bin.getRight() instanceof Identifier) {
                Double val = toDouble(((Constant) bin.getLeft()).getValue());
                Identifier y = (Identifier) bin.getRight();
                if (val != null) {
                    updated.add(new Inequality(1, id, -1, y, val));
                    updated.add(new Inequality(-1, id, 1, y, -val));
                }
            }
        } else if (bin.getOperator() instanceof SubtractionOperator) {
            if (bin.getLeft() instanceof Identifier && bin.getRight() instanceof Constant) {
                Identifier y = (Identifier) bin.getLeft();
                Double val = toDouble(((Constant) bin.getRight()).getValue());
                if (val != null) {
                    updated.add(new Inequality(1, id, -1, y, -val));
                    updated.add(new Inequality(-1, id, 1, y, val));
                }
            } else if (bin.getLeft() instanceof Constant && bin.getRight() instanceof Identifier) {
                Double val = toDouble(((Constant) bin.getLeft()).getValue());
                Identifier y = (Identifier) bin.getRight();
                if (val != null) {
                    updated.add(new Inequality(1, id, 1, y, val));
                    updated.add(new Inequality(-1, id, -1, y, -val));
                }
            }
        }
    }

    return new TwoVariablesLinearInequality(updated);
}

    private static Double selfShiftDelta(Identifier id, SymbolicExpression l, SymbolicExpression r) {
        if (l instanceof Identifier && ((Identifier) l).equals(id) && r instanceof Constant)
            return toDouble(((Constant) r).getValue());
        if (r instanceof Identifier && ((Identifier) r).equals(id) && l instanceof Constant)
            return toDouble(((Constant) l).getValue());
        return null;
    }

    private TwoVariablesLinearInequality shift(Identifier id, double delta) {
        Set<Inequality> updated = new HashSet<>();
        for (Inequality ineq : inequalities) {
            if (ineq.getX() != null && ineq.getX().equals(id)) {
                updated.add(new Inequality(ineq.getA(), ineq.getX(), ineq.getB(), ineq.getY(),
                        ineq.getC() + ineq.getA() * delta));
            } else if (ineq.getY() != null && ineq.getY().equals(id)) {
                updated.add(new Inequality(ineq.getA(), ineq.getX(), ineq.getB(), ineq.getY(),
                        ineq.getC() + ineq.getB() * delta));
            } else {
                updated.add(ineq);
            }
        }
        return new TwoVariablesLinearInequality(updated);
    }

    private TwoVariablesLinearInequality reflect(Identifier id, double c) {
        Set<Inequality> updated = new HashSet<>();
        for (Inequality ineq : inequalities) {
            if (ineq.getX() != null && ineq.getX().equals(id)) {
                updated.add(new Inequality(-ineq.getA(), ineq.getX(), ineq.getB(), ineq.getY(),
                        ineq.getC() - ineq.getA() * c));
            } else if (ineq.getY() != null && ineq.getY().equals(id)) {
                updated.add(new Inequality(ineq.getA(), ineq.getX(), -ineq.getB(), ineq.getY(),
                        ineq.getC() - ineq.getB() * c));
            } else {
                updated.add(ineq);
            }
        }
        return new TwoVariablesLinearInequality(updated);
    }

    @Override
    public TwoVariablesLinearInequality smallStepSemantics(ValueExpression expression, ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {
            return this;
    }

    @Override
    public TwoVariablesLinearInequality assume(ValueExpression expression, ProgramPoint src, ProgramPoint dest,
        SemanticOracle oracle) throws SemanticException {
    if (isBottom()) return this;
    if (!(expression instanceof BinaryExpression)) return this;

    BinaryExpression bin = (BinaryExpression) expression;
    SymbolicExpression left = bin.getLeft();
    SymbolicExpression right = bin.getRight();
    it.unive.lisa.symbolic.value.operator.binary.BinaryOperator operator = bin.getOperator();

    Set<Inequality> updated = new HashSet<>(inequalities);

    if (left instanceof Identifier && right instanceof Identifier) {
        Identifier x = (Identifier) left;
        Identifier y = (Identifier) right;

        if (operator instanceof ComparisonLe || operator instanceof ComparisonLt) {
            // x <= y  donc  x - y =< 0
            // x < y   donc egalement x - y ≤ 0
            updated.add(new Inequality(1, x, -1, y, 0));
        } else if (operator instanceof ComparisonGe || operator instanceof ComparisonGt) {
            // x >= y  donc  y - x =< 0
            updated.add(new Inequality(-1, x, 1, y, 0));
        }

    } else if (left instanceof Identifier && right instanceof Constant) {
        Identifier x = (Identifier) left;
        Double val = toDouble(((Constant) right).getValue());
        if (val != null) {
            if (operator instanceof ComparisonLe || operator instanceof ComparisonLt) {
                // x <= c  donc  x =< c
                updated.add(new Inequality(1, x, 0, null, val));
            } else if (operator instanceof ComparisonGe || operator instanceof ComparisonGt) {
                // x >= c  donc  -x =< -c
                updated.add(new Inequality(-1, x, 0, null, -val));
            }
        }

    } else if (left instanceof Constant && right instanceof Identifier) {
        Identifier y = (Identifier) right;
        Double val = toDouble(((Constant) left).getValue());
        if (val != null) {
            if (operator instanceof ComparisonLe || operator instanceof ComparisonLt) {
                // c <= y  donc  -y =< -c
                updated.add(new Inequality(-1, y, 0, null, -val));
            } else if (operator instanceof ComparisonGe || operator instanceof ComparisonGt) {
                // c >= y  donc  y =< c
                updated.add(new Inequality(1, y, 0, null, val));
            }
        }
    }

    return new TwoVariablesLinearInequality(updated);
}

    @Override
    public boolean knowsIdentifier(Identifier id) {
        if (isTop() || isBottom()) return false;
        for (Inequality ineq : inequalities) {
            if (ineq.involves(id)) return true;
        }
        return false;
    }
    @Override
    public TwoVariablesLinearInequality forgetIdentifier(Identifier id) throws SemanticException {
        if (isTop() || isBottom()) return this;
        Set<Inequality> remaining = new HashSet<>();
        for (Inequality ineq : inequalities) {
            if (!ineq.involves(id)) remaining.add(ineq);
        }
        return new TwoVariablesLinearInequality(remaining);
    }

    @Override
    public TwoVariablesLinearInequality forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        if (isTop() || isBottom()) return this;
        Set<Inequality> remaining = new HashSet<>();
        for (Inequality ineq : inequalities) {
            boolean xMatches = ineq.getX() != null && test.test(ineq.getX());
            boolean yMatches = ineq.getY() != null && test.test(ineq.getY());
            if (!xMatches && !yMatches) {
                remaining.add(ineq);
            }
        }
        return new TwoVariablesLinearInequality(remaining);
    }

    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle)
        throws SemanticException {
    if (isBottom()) return Satisfiability.BOTTOM;
    if (isTop()) return Satisfiability.UNKNOWN;
    if (!(expression instanceof BinaryExpression)) return Satisfiability.UNKNOWN;

    BinaryExpression bin = (BinaryExpression) expression;
    SymbolicExpression left = bin.getLeft();
    SymbolicExpression right = bin.getRight();
    it.unive.lisa.symbolic.value.operator.binary.BinaryOperator operator = bin.getOperator();

    Inequality target = null;

    if (left instanceof Identifier && right instanceof Identifier) {
        Identifier x = (Identifier) left;
        Identifier y = (Identifier) right;

        if (operator instanceof ComparisonLe || operator instanceof ComparisonLt) {
            target = new Inequality(1, x, -1, y, 0);
        } else if (operator instanceof ComparisonGe || operator instanceof ComparisonGt) {
            target = new Inequality(-1, x, 1, y, 0);
        }

    } else if (left instanceof Identifier && right instanceof Constant) {
        Identifier x = (Identifier) left;
        Double val = toDouble(((Constant) right).getValue());
        if (val != null) {
            if (operator instanceof ComparisonLe || operator instanceof ComparisonLt) {
                target = new Inequality(1, x, 0, null, val);
            } else if (operator instanceof ComparisonGe || operator instanceof ComparisonGt) {
                target = new Inequality(-1, x, 0, null, -val);
            }
        }
    }

    if (target != null && implies(target)) {
        return Satisfiability.SATISFIED;
    }

    return Satisfiability.UNKNOWN;
}

    @Override
    public StructuredRepresentation representation() {
        if(isBottom)
            return Lattice.bottomRepresentation();
        if(isTop())
            return Lattice.topRepresentation();
        return new StringRepresentation(inequalities.toString());
    }
    
    @Override
    public TwoVariablesLinearInequality pushScope(ScopeToken token) throws SemanticException {
        return this;
    }

    @Override
    public TwoVariablesLinearInequality popScope(ScopeToken token) throws SemanticException {
        return this;
    }
    @Override
    public boolean lessOrEqual(TwoVariablesLinearInequality other) throws SemanticException {
        if (isBottom() || other.isTop()) return true;
        if (isTop()) return other.isTop();
        if (other.isBottom()) return false;

        for (Inequality ineq : other.inequalities) {
            if (!this.implies(ineq)) return false;
        }
        return true;
    }

    @Override
    public TwoVariablesLinearInequality lub(TwoVariablesLinearInequality other) throws SemanticException {
        if (isTop() || other.isTop()) return top();
        if (isBottom()) return other;
        if (other.isBottom()) return this;

        Map<String, Inequality> thisMap = new HashMap<>();
        for (Inequality ineq : this.inequalities) {
            thisMap.merge(ineq.getKey(), ineq, (old, nw) -> old.getC() <= nw.getC() ? old : nw);
        }

        Map<String, Inequality> otherMap = new HashMap<>();
        for (Inequality ineq : other.inequalities) {
            otherMap.merge(ineq.getKey(), ineq, (old, nw) -> old.getC() <= nw.getC() ? old : nw);
        }

        Set<Inequality> result = new HashSet<>();
        for (Map.Entry<String, Inequality> entry : thisMap.entrySet()) {
            Inequality otherIneq = otherMap.get(entry.getKey());
            if (otherIneq != null) {
                Inequality thisIneq = entry.getValue();
                double maxC = Math.max(thisIneq.getC(), otherIneq.getC());
                result.add(new Inequality(thisIneq.getA(), thisIneq.getX(),
                        thisIneq.getB(), thisIneq.getY(), maxC));
            }
        }

        if (result.isEmpty()) return top();
        return new TwoVariablesLinearInequality(result);
    }

    @Override
    public TwoVariablesLinearInequality widening(TwoVariablesLinearInequality other) throws SemanticException {
        if (isBottom()) return other;
        if (other.isBottom()) return this;
        if (isTop() || other.isTop()) return top();

        Set<Inequality> stable = new HashSet<>();
        for (Inequality ineq : this.inequalities) {
            if (other.implies(ineq)) stable.add(ineq);
        }

        if (stable.isEmpty()) return top();
        return new TwoVariablesLinearInequality(stable);
    }

    @Override
    public TwoVariablesLinearInequality top() {
        return TOP;
    }
    @Override
    public TwoVariablesLinearInequality bottom() {
        return BOTTOM;
    }

    public boolean isTop() {
        return !isBottom && inequalities.isEmpty();
    }

    public boolean isBottom() {
        return isBottom;
    }

    private boolean implies(Inequality ineq) {
        for (Inequality existing : inequalities) {
            if (existing.implies(ineq)) return true;
        }
        return false;
    }

    private static Double toDouble(Object value) {
    if (value instanceof Integer) return ((Integer) value).doubleValue();
    if (value instanceof Float) return ((Float) value).doubleValue();
    if (value instanceof Double) return (Double) value;
    return null;}

    public static final class Inequality {

        private final int a;
        private final Identifier x;   
        private final int b;
        private final Identifier y;  
        private final double c;

        public Inequality(int a, Identifier x, int b, Identifier y, double c) {
            if (x == null && y == null)
                throw new IllegalArgumentException("At least one variable must be present");
            if (a == 0 && x != null)
                throw new IllegalArgumentException("Coefficient of x cannot be 0 if x is present");
            if (b == 0 && y != null)
                throw new IllegalArgumentException("Coefficient of y cannot be 0 if y is present");

            this.a = a;
            this.x = x;
            this.b = b;
            this.y = y;
            this.c = c;
        }

        public int getA() {
            return a;
        }

        public Identifier getX() {
            return x;
        }

        public int getB() {
            return b;
        }

        public Identifier getY() {
            return y;
        }

        public double getC() {
            return c;
        }

        public boolean involves(Identifier id) {
            return (x != null && x.equals(id)) || (y != null && y.equals(id));
        }

        public boolean isUnary() {
            return x == null || y == null;
        }

        public boolean isContradiction() {
            return x == null && y == null && c < 0;
        }

        public boolean isTrivial() {
            return x == null && y == null && c >= 0;
        }

        public Inequality without(Identifier id) {
            if (x != null && x.equals(id))
                return y == null ? null : new Inequality(b, y, 0, null, c);
            if (y != null && y.equals(id))
                return x == null ? null : new Inequality(a, x, 0, null, c);
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Inequality))
                return false;
            Inequality other = (Inequality) obj;
            return a == other.a && b == other.b
                    && Double.compare(c, other.c) == 0
                    && java.util.Objects.equals(x, other.x)
                    && java.util.Objects.equals(y, other.y);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(a, x, b, y, c);
        }

        @Override
        public String toString() {
            String left;
            if (x != null && y != null)
                left = a + "*" + x + " + " + b + "*" + y;
            else if (x != null)
                left = a + "*" + x;
            else
                left = b + "*" + y;

            return left + " <= " + c;
        }

        public boolean implies(Inequality other) {
            return this.a == other.a && this.b == other.b
                    && java.util.Objects.equals(this.x, other.x)
                    && java.util.Objects.equals(this.y, other.y)
                    && this.c <= other.c;
        }

        public String getKey() {
            return a + "," + (x != null ? x.toString() : "null") + ","
                    + b + "," + (y != null ? y.toString() : "null");
        }
    }
}