package pl.mwasyluk.ouroom_server.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdentifiableTest {
    @Test
    @DisplayName("new instance always has an id")
    void newInstanceAlwaysHasAnId() {
        Identifiable identifiable = new Identifiable() {
        };

        assertNotNull(identifiable.getId());
    }

    @Test
    @DisplayName("new instances always have different ids")
    void newInstancesAlwaysHaveDifferentIds() {
        Identifiable i1 = new Identifiable() {
        };
        Identifiable i2 = new Identifiable() {
        };
        Identifiable i3 = new Identifiable() {
        };

        assertAll(() -> {
            assertNotEquals(i1.getId(), i2.getId());
            assertNotEquals(i1.getId(), i3.getId());
            assertNotEquals(i2.getId(), i3.getId());
        });
    }
}
