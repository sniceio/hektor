package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author jonas@jonasborjesson.com
 */
public class BufferingActorContextTest {

    @Mock
    private InternalHektor hektor;

    @Mock
    private Actor defaultActor;

    @Mock
    private ActorRef defaultActorRef;

    private ActorBox defaultActorBox;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        defaultActorBox = ActorBox.create(new DefaultMailBox(), defaultActor, defaultActorRef);
    }

    @Test
    public void testLookupActorDoesntExist() throws Exception {
        when(hektor.lookupActorBox(any(ActorPath.class))).thenReturn(Optional.empty());
        final BufferingActorContext ctx = new BufferingActorContext(hektor, defaultActorBox, ActorRef.None());
        final Optional<ActorRef> ref =  ctx.lookup("doesnt/exist");
        assertThat(ref.isPresent(), is(false));
    }

    @Test
    public void testLookupActorExists() throws Exception {
        when(hektor.lookupActorBox(any(ActorPath.class))).thenReturn(Optional.of(defaultActorBox));
        final BufferingActorContext ctx = new BufferingActorContext(hektor, defaultActorBox, ActorRef.None());
        final Optional<ActorRef> ref =  ctx.lookup("yes/exist");
        assertThat(ref.isPresent(), is(true));
    }

}