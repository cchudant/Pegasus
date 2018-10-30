package fr.ftnl.listener;

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
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * The FTNL listener manager.
 * 
 * @see #register(Object)
 */
public class ListenerManager implements EventListener {
  private static final Logger LOG = LogManager.getLogger();
  private static final Gauge METRICS_RUNNING = Gauge.build()
    .name("listeners_running")
    .help("Running listeners")
    .labelNames("listener", "thread")
    .register();
  private static final Histogram METRICS_LATENCY = Histogram.build()
    .name("listeners_latency")
    .help("Request latency")
    .labelNames("listener", "thread")
    .register();
  private final FTNL main;
  private final List<Listener> listeners = new ArrayList<>();
  
  public ListenerManager(FTNL main) {
    this.main = main;
  }

  /**
   * Register handlers.
   * The instance will be searched for event handlers.
   * 
   * An event handler method must be public, be annotated by the {@link EventHandler} annotation,
   * and have exactly one parameter which is a subclass of {@link Event}.
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
      if (!method.isAnnotationPresent(EventHandler.class))
        continue;

      if (method.getParameterCount() != 1)
        throw new ListenerRegisterException("listener must have exactly one parameter");

      Class<?> param = method.getParameters()[0].getType();
      if (!Event.class.isAssignableFrom(param))
        throw new ListenerRegisterException("listener parameter must be a subclass of Event");

      Listener listener = new Listener(main, param.asSubclass(Event.class), method, instance);

      LOG.info("Registering {}", listener);
      listeners.add(listener);
    }
  }

  /**
   * Dispatch an event to the listeners.
   * 
   * @param event the event
   * @see #register(Object)
   */
  @Override
  public void onEvent(Event event) {
    String threadName = Thread.currentThread().getName();

    for (Listener listener : listeners) {
      if (event.getClass() != listener.getEventClass())
        continue;

      LOG.debug("Dispatching event to listener {}", listener);

      // Metrics
      METRICS_RUNNING.labels(listener.toString(), threadName)
        .inc();
      Histogram.Timer timer = METRICS_LATENCY.labels(listener.toString(), threadName)
        .startTimer();

      // Invoke
      Object ret = null;
      try {
        ret = listener.getMethod().invoke(listener.getInstance(), event);
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
          LOG.debug("Finished dispatching event for {}", listener);
          METRICS_RUNNING.labels(listener.toString(), threadName)
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
   * @return the listeners
   */
  public List<? super Listener> getListeners() {
    return listeners;
  }
}