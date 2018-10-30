package fr.ftnl.modules;

import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import fr.ftnl.FTNL;
import fr.ftnl.command.Category;
import fr.ftnl.command.CommandHandler;
import fr.ftnl.listener.EventHandler;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public final class LicorneModule {
  private static final Logger LOG = LogManager.getLogger();
  private final FTNL main;

  public LicorneModule(FTNL main) {
    this.main = main;
  }

  @EventHandler
  public void onReady(ReadyEvent event) {
    LOG.info("shard ready!");
  }

  @CommandHandler(name = "licorne", category = Category.FUN)
  public void licorneCommand(MessageReceivedEvent event) {
    throw new RuntimeException("Les licornes existent");
    // will be caught by the event dispatcher
  }

  @CommandHandler(name = "echo", category = Category.FUN)
  public CompletionStage<?> test(MessageReceivedEvent event) {
    Message msg = event.getMessage();

    return msg.getChannel().sendMessage(msg.getContentRaw())
      .submit() //do this async action
      .thenComposeAsync(sent -> { //then run this
        Document document = new Document();
        document.append("licorne", "i like trains");
        main.getDatabase().getCollection("licorne")
          .insertOne(document);

        return sent.getChannel().sendMessage("Written to mongodb")
          .submit(); //compose with this async action
      })
      .thenRunAsync(() -> { //and then run this
        LOG.debug("everything sent, now i'm hungry.");
      });
  }

  @CommandHandler(name = "say")
  public CompletionStage<?> say(MessageReceivedEvent event) {
    Message msg = event.getMessage();

    return msg.getChannel().sendMessage(msg.getContentRaw())
      .submit();
  }
}