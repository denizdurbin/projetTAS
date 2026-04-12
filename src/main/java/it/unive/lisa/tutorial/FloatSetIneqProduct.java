package it.unive.lisa.tutorial;

import it.unive.lisa.analysis.combination.ValueCartesianProduct;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;

public class FloatSetIneqProduct
        extends ValueCartesianProduct<ValueEnvironment<FloatConstantSetDomain>, TwoVariablesLinearInequality> {

    public FloatSetIneqProduct() {
        this(new ValueEnvironment<>(new FloatConstantSetDomain((Float) null)).top(),
                new TwoVariablesLinearInequality().top());
    }

    public FloatSetIneqProduct(
            ValueEnvironment<FloatConstantSetDomain> left,
            TwoVariablesLinearInequality right) {
        super(left, right);
    }

    @Override
    public FloatSetIneqProduct mk(
            ValueEnvironment<FloatConstantSetDomain> left,
            TwoVariablesLinearInequality right) {
        return new FloatSetIneqProduct(left, right);
    }
}
