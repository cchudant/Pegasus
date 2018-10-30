package fr.ftnl.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.ftnl.FTNL;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * The FTNL listener manager.
 * 
 * @see #register(Object)
 */
public class CommandManager implements EventListener {
  private static final Logger LOG = LogManager.getLogger();
  private static final Gauge METRICS_RUNNING = Gauge.build()
    .name("commands_running")
    .help("Running listeners")
    .labelNames("listener", "thread")
    .register();
  private static final Histogram METRICS_LATENCY = Histogram.build()
    .name("commands_latency")
    .help("Request latency")
    .labelNames("listener", "thread")
    .register();
  private final FTNL main;
  private final List<Command> commands = new ArrayList<>();
  
  public CommandManager(FTNL main) {
    this.main = main;
  }

  /**
   * Register handlers.
   * The instance will be searched for commands handlers.
   * 
   * A command handler method must be public, be annotated by the {@link CommandHandler} annotation,
   * and have exactly one parameter which is of type {@link Event}.
   * 
   * Some metrics will be reported, including the response time and the number of concurrent
   * handlers running at a given time.
   * The event handler must return a subclass of {@link CompletionStage} if any asynchronous
   * code is ran.
   * 
   * @param instance the instance
   * @throws ListenerRegisterException if an event handler is not properly defined
   */
  public void register(@Nonnull Object instance) {
    Class<?> clazz = instance.getClass();
    for (Method method : clazz.getMethods()) {
      if (!method.isAnnotationPresent(CommandHandler.class))
        continue;

      if (method.getParameterCount() != 1)
        throw new CommandRegisterException("command must have exactly one parameter");

      Class<?> param = method.getParameters()[0].getType();
      if (param != MessageReceivedEvent.class)
        throw new CommandRegisterException("command parameter must be of type MessageReceivedEvent");

      CommandHandler annotation = method.getAnnotation(CommandHandler.class);
      Command listener = new Command(main, annotation, method, instance);

      LOG.info("Registering {}", listener);
      commands.add(listener);
    }
  }

  /**
   * Dispatch an event to the commands.
   * 
   * @param event the event
   * @see #register(Object)
   */
  @Override
  public void onEvent(Event event) {
    if (event.getClass() != MessageReceivedEvent.class)
      return;
    MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;

    String threadName = Thread.currentThread().getName();

    for (Command command : commands) {
      if (!command.match(msgEvent))
        continue;

      LOG.info("Dispatching event to command {}", command);

      // Metrics
      METRICS_RUNNING.labels(command.toString(), threadName)
        .inc();
      Histogram.Timer timer = METRICS_LATENCY.labels(command.toString(), threadName)
        .startTimer();

      // Invoke
      Object ret = null;
      try {
        ret = command.getMethod().invoke(command.getInstance(), event);
      } catch (InvocationTargetException ex) {
        // Exception in listener's code
        //todo better logging
        LOG.error(ex.getTargetException());
        continue;
      } catch (IllegalAccessException ex) {
        // Should never happen
        throw new RuntimeException(ex);
      } finally {
        // Metrics
        Runnable endMetrics = () -> {
          LOG.debug("Finished dispatching event for {}", command);
          METRICS_RUNNING.labels(command.toString(), threadName)
            .dec();
          timer.observeDuration();
        };

        if (ret instanceof CompletionStage) {
          CompletionStage<?> future = (CompletionStage<?>) ret;
          future.thenRunAsync(endMetrics);
        } else {
          endMetrics.run();
        }
      }
    }
  }

  /**
   * @return the commands
   */
  public List<? super Command> getCommands() {
    return commands;
  }
}