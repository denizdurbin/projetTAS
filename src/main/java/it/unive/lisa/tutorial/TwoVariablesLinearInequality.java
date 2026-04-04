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
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assign'");
    }
    @Override
    public TwoVariablesLinearInequality smallStepSemantics(ValueExpression expression, ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {
            return this;
    }
    @Override
    public TwoVariablesLinearInequality assume(ValueExpression expression, ProgramPoint src, ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assume'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'satisfies'");
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