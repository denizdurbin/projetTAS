package it.unive.lisa.tutorial;

import java.util.function.Predicate;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.lattices.Satisfiability;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class TwoVariablesLinearEquality implements ValueDomain<TwoVariablesLinearEquality> {

    private static final TwoVariablesLinearEquality TOP = new TwoVariablesLinearEquality(true);
    private static final TwoVariablesLinearEquality BOTTOM = new TwoVariablesLinearEquality(false);
    public TwoVariablesLinearEquality(boolean b) {
        //TODO Auto-generated constructor stub
    }
    @Override
    public TwoVariablesLinearEquality assign(Identifier id, ValueExpression expression, ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assign'");
    }
    @Override
    public TwoVariablesLinearEquality smallStepSemantics(ValueExpression expression, ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'smallStepSemantics'");
    }
    @Override
    public TwoVariablesLinearEquality assume(ValueExpression expression, ProgramPoint src, ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assume'");
    }
    @Override
    public boolean knowsIdentifier(Identifier id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'knowsIdentifier'");
    }
    @Override
    public TwoVariablesLinearEquality forgetIdentifier(Identifier id) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forgetIdentifier'");
    }
    @Override
    public TwoVariablesLinearEquality forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forgetIdentifiersIf'");
    }
    @Override
    public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'satisfies'");
    }
    @Override
    public StructuredRepresentation representation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'representation'");
    }
    @Override
    public TwoVariablesLinearEquality pushScope(ScopeToken token) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pushScope'");
    }
    @Override
    public TwoVariablesLinearEquality popScope(ScopeToken token) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'popScope'");
    }
    @Override
    public boolean lessOrEqual(TwoVariablesLinearEquality other) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lessOrEqual'");
    }
    @Override
    public TwoVariablesLinearEquality lub(TwoVariablesLinearEquality other) throws SemanticException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lub'");
    }
    @Override
    public TwoVariablesLinearEquality top() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'top'");
    }
    @Override
    public TwoVariablesLinearEquality bottom() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bottom'");
    }

    
    
}
