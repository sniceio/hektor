package io.hektor.core.internal;

import io.hektor.core.ActorPath;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/*is*
 * @author jonas@jonasborjesson.com
 */
public class DefaultActorPathTest {

    @Test
    public void testCreateActorPath() {
        final ActorPath root = new DefaultActorPath(null, "root");
        assertThat(root.toString(), is("/root"));
        assertThat(root.isRoot(), is(true));

        final ActorPath child = root.createChild("child");
        assertThat(child.toString(), is("/root/child"));
        assertThat(child.isRoot(), is(false));

        final ActorPath grandChild = child.createChild("grand-child");
        assertThat(grandChild.toString(), is("/root/child/grand-child"));
        assertThat(grandChild.isRoot(), is(false));
    }

    @Test
    public void testHashCodeEquals() {
        final ActorPath root1 = new DefaultActorPath(null, "root");
        final ActorPath root2 = new DefaultActorPath(null, "root");
        final ActorPath root3 = new DefaultActorPath(null, "root3");
        assertPath(root1, root2, true);
        assertPath(root2, root3, false);

        final ActorPath child1 = new DefaultActorPath(root1, "hello");
        final ActorPath child2 = new DefaultActorPath(root2, "hello");
        final ActorPath child3 = new DefaultActorPath(root2, "world");
        assertPath(child1, child1, true);
        assertPath(child1, child2, true);
        assertPath(child1, child3, false);
        assertPath(child2, child3, false);

        final ActorPath grandChild1 = new DefaultActorPath(child1, "foo");
        assertPath(child1, grandChild1, false);
    }

    @Test
    public void testGetRoot() throws Exception {
        final ActorPath path = DefaultActorPath.create(null, "/one/two/three");
        assertThat(path.isRoot(), is(false));

        ActorPath root = path.getRoot();
        assertThat(root.isRoot(), is(true));
        assertThat(root.name(), is("one"));
        assertThat(root.parent().isPresent(), is(false));
        assertThat(root.getRoot(), is(root));

        final ActorPath child = DefaultActorPath.create(path, "/child/hello");
        assertThat(child.isRoot(), is(false));
        assertThat(path.getRoot(), is(child.getRoot()));
    }

    @Test
    public void testCreatePathFromAbsolute() throws Exception {
        assertPath("/hello/world");
        assertPath("/hello");
        assertPath("/hello/world/again/can/go/on/for/forever");
        assertPath("/hello/", "/hello");
    }

    @Test
    public void testCreatePathFromRelative() throws Exception {
        assertPath("/hello/world", "../foo", "/hello/foo");
        assertPath("/hello/world/one/two/three", "../foo", "/hello/world/one/two/foo");
        assertPath("/hello/world/one/two/three", "../../foo", "/hello/world/one/foo");
        assertPath("/hello/world/one/two/three", "../../../foo", "/hello/world/foo");

        // Should be fine to specify just ./ as well. Perhaps not much
        // use but make sense anyway.
        assertPath("/hello/world", "./foo", "/hello/world/foo");
    }

    private void assertPath(final String path) {
        assertPath(path, path);
    }

    private void assertPath(final String parentPath, final String relativePath, final String expected) {
        final ActorPath parent = DefaultActorPath.create(null, parentPath);
        final ActorPath actualPath = DefaultActorPath.create(parent, relativePath);
        assertThat(actualPath.toString(), is(expected));
    }

    private void assertPath(final String path, final String expected) {
        final ActorPath actorPath = DefaultActorPath.create(null, path);
        assertThat(actorPath.toString(), is(expected));
    }

    private void assertPath(final ActorPath path1, final ActorPath path2, final boolean equals) {
        if (equals) {
            assertThat(path1, is(path2));
            assertThat(path2, is(path1));
            assertThat(path1.hashCode(), is(path2.hashCode()));
            assertThat(path2.hashCode(), is(path1.hashCode()));
        } else {
            assertThat(path1, not(path2));
            assertThat(path2, not(path1));
            assertThat(path1.hashCode(), not(path2.hashCode()));
            assertThat(path2.hashCode(), not(path1.hashCode()));
        }
    }
}