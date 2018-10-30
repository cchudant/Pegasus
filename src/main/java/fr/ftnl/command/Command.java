package fr.ftnl.command;

import java.lang.reflect.Method;

import fr.ftnl.FTNL;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Command {
  private final FTNL main;
  private final CommandHandler annotation;
  private final Method method;
  private final Object instance;

  Command(FTNL main, CommandHandler annotation, Method method, Object instance) {
    this.main = main;
    this.annotation = annotation;
    this.method = method;
    this.instance = instance;
  }

  /**
   * Match a message content against this command.
   * 
   * @return true if the message content invokes the command 
   */
  public boolean match(MessageReceivedEvent event) {
    Message msg = event.getMessage();

    if (!annotation.allowBot() && msg.getAuthor().isBot())
      return false;
    
    if (!annotation.allowSelf() && msg.getJDA().getSelfUser() == msg.getAuthor())
      return false;
    
    return msg.getContentRaw()
      .startsWith(main.getConfig().getPrefix() + annotation.name());
  }

  /**
   * @return the listener name
   */
  @Override
  public String toString() {
    return String.format(
      "%s.%s(...)",
      this.instance.getClass().getCanonicalName(),
      this.annotation.name()
    );
  }

  /**
   * @return the annotation
   */
  public CommandHandler getAnnotation() {
    return annotation;
  }

  /**
   * @return the method
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return the instance
   */
  public Object getInstance() {
    return instance;
  }
}