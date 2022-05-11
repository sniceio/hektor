package io.hektor.fsm.docs;

import static io.snice.preconditions.PreConditions.assertNotEmpty;

/**
 * Simple label, which various tools can use when they generate documentation.
 */
public class Label {

    private final String label;

    private Label(final String label) {
        this.label = label;
    }

    public static Label label(final String label) {
        assertNotEmpty(label);
        return new Label(label);
    }

    @Override
    public String toString() {
        return label;
    }
}
