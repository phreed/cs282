/**
 * 
 */
package edu.vanderbilt.cs282.feisele.annotation;

/**
 * Used by the testing framework to determine which tests should be run and
 * when.
 * <p>
 * <code>
 * @TestPreamble ( triggers = {"smoke","full"}, </code>
 * <code>conception = "1.6.2" activate =
 *               "1.6.3", expire = "unlimited", units = {""} ) </code>
 *               <p>
 *               on* attributes indicate what triggers the test:
 */
public @interface TestPreamble {

    /** for any change to the system */
    boolean onSmoke() default true;

    /** for any change to one of the indicated units */
    String[] onUnit();

    /** for any change to one of the indicated components */
    String[] onComponent();

    /**
     * Indicates when the test is relevant. The test is not required to pass at
     * this point but is made available for developers to work against. In the
     * case of test driven development this indicates when
     */
    String conception() default "0.0.0";

    /**
     * Indicates when the test should become active. The test is expected to
     * pass starting at this version.
     */
    String activate() default "0.0.0";
    
    /**
     * Indicates when the item being tested is no longer expected to run.
     * Generally the test would fail if run
     */
    String deprecate() default "9999.0.0";
    
    /**
     * Indicates when the test no longer be invoked.
     * Generally the test would fail if run
     */
    String expire() default "9999.0.0";
}
