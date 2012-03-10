package an.xacml.engine.evaluator;

public interface Comparator {

    /**
     * Compare Object a and b. If a equals to b, return 0; if a less than b, return -1; if a greater than b, return 1.
     * @param a
     * @param b
     * @return 0 if a equals b; -1 if a less than b; 1 if a greater than b.
     */
    public int compare(Object a, Object b);
}
