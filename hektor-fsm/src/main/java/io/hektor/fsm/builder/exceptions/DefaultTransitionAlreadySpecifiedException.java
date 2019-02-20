package io.hektor.fsm.builder.exceptions;

/**
 * You can only have a single default transition for a transient state. If you try and define
 * another state you will be getting this exception.
 *
 * @author jonas@jonasborjesson.com
 */
public class DefaultTransitionAlreadySpecifiedException extends StateBuilderException {

   public DefaultTransitionAlreadySpecifiedException(final Enum state) {
       super(state, "A transient state can only have a single default transition");
   }

}
