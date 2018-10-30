package fr.ftnl.listener;

import java.lang.reflect.Method;

import fr.ftnl.FTNL;
import net.dv8tion.jda.core.events.Event;

public class Listener {
  private final FTNL main;
  private final Class<? extends Event> eventClass;
  private final Method method;
  private final Object instance;

  Listener(FTNL main, Class<? extends Event> eventClass, Method method, Object instance) {
    this.main = main;
    this.eventClass = eventClass;
    this.method = method;
    this.instance = instance;
  }

  /**
   * @return the listener name
   */
  @Override
  public String toString() {
    return String.format(
      "%s.%s(%s)",
      this.instance.getClass().getCanonicalName(),
      this.method.getName(),
      this.eventClass.getSimpleName()
    );
  }

  /**
   * @return the eventClass
   */
  public Class<? extends Event> getEventClass() {
    return eventClass;
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