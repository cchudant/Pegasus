package fr.ftnl.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {
  /**
   * The name of the command
   * @return the name of the command
   */
  String name();

  /**
   * The category of the command
   * @return the category of the command
   */
  Category category() default Category.HIDDEN;

  /**
   * Will the listener trigger if the sender is the bot itself?
   * @return whether the listener will trigger if the sender is the bot itself
   */
  boolean allowSelf() default false;

  /**
   * Will the listener trigger if the sender is a bot
   * @return whether the listener will trigger if the sender is a bot
   */
  boolean allowBot() default false;
}
