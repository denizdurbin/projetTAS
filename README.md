# Design Choices and Justifications

This document summarizes our implementation choices, focusing on places where multiple designs were possible (when the spec left things up to the implementer) and explaining why we chose our particular approach.

## 1. `FloatConstantSetDomain`

### Concrete semantics
We use IEEE 754 float semantics, not mathematical reals. In other words, the domain models actual machine float behavior.

Because of that:
- `+Infinity` and `-Infinity` are legitimate values that the domain can store, since they have well-defined arithmetic behavior such as `Infinity + 1 = Infinity`.
- `NaN` is treated as “we do not know what this is anymore” so any operation producing `NaN` sends the result to **TOP**.

One consequence is that the optimization from Section 4.4 of the course,

> `TOP × {0} = {0}`

doesn't hold in our domain. Under IEEE 754, `Infinity × 0 = NaN`, so in our semantics:

> `TOP × {0} = TOP`

We confirmed this design choice with the professor.

### Representation
We represent the domain using a single `Set<Float>` field:
- `null` means **TOP**
- the empty set means **BOTTOM**
- any non-empty set means a finite set of concrete float values

An alternative would have been to use separate boolean flags, as in `InfiniteSetOfConcreteValues` from the tutorial repository.

### Constructor ambiguity
We have both a `Set<Float>` constructor and a `Float` constructor, so `null` is ambiguous between the two.

Because of this, the `TOP` constant uses an explicit cast:

```java
(Set<Float>) null
```

### No widening override
We do not override widening.

Since the set is bounded at `N` elements, the lattice is finite which makes it so the default widening (just `lub`), is enough to guarantee termination.

## 2. `TwoVariablesLinearInequality`

### Flat set of inequalities instead of per-pair projections
The Simon/King/Howe paper stores a 2D polyhedron for each variable pair and then runs completion to keep all projections consistent.

We chose a simpler representation: just a flat set of inequalities, with implication checks done syntactically.

This loses some precision on joins and entailment. For example, we can't infer `x ≤ 5` from `2*x ≤ 10`.

### `assign` on self-referential updates: shift in place
A straightforward implementation of `assign(id, expr)` would:
1. drop all constraints mentioning `id`
2. derive new ones from the assignment

But this behaves badly for updates such as:

```text
i = i + 1
```

That naive approach can produce constraints such as:

```text
0 ≤ -1
```

which is a contradiction. After widening, `i` then tends to disappear completely.

To avoid that, we do updates of the form:

```text
i = i ± c
```

and shift existing constraints in place.

So a bound of the form:

```text
a*i + b*y ≤ k
```

becomes:

```text
a*i + b*y ≤ k + a*delta
```

This is exact for constant shifts and preserves useful loop counter information.

### Strict vs non-strict comparisons
We encode:

```text
x < y
```

as:

```text
x - y ≤ 0
```

This is sound under real valued semantics, but it is one step weaker than the precise integer encoding:

```text
x - y ≤ -1
```

We chose the simpler version because the domain already uses `double` constants.

### No constraint normalisation
We do not normalize constraints globally.

That means two inequalities with the same shape but different bounds can both remain in the set, and only `lub` merges them.

A normalization pass would reduce redundancy and improve precision, but our benchmarks did not need it.

## 3. `FloatSetIneqProduct` (Cartesian product)

### Non-communicating product
We implement a plain Cartesian product by extending `ValueCartesianProduct` and inheriting its default behavior.

## Summary
Overall, our choices favored:
- concrete IEEE 754 behavior over idealized real-number semantics
- smaller and simpler implementations over maximum precision
- exact handling for a few important special cases, especially loop-counter updates

