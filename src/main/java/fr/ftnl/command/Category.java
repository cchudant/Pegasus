package fr.ftnl.command;

public enum Category {
  MUSIC("music", "Affiche l'aide pour la musique"),
  FUN("fun", "Affiche l'aide pour les commandes fun"),
  HIDDEN(true);

  private final String name;
  private final String desc;
  private final boolean hidden;

  Category(String name, String desc, boolean hidden) {
    this.name = name;
    this.desc = desc;
    this.hidden = hidden;
  }

  Category(String name, String desc) {
    this(name, desc, false);
  }

  Category(boolean hidden) {
    this(null, null, true);
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the desc
   */
  public String getDesc() {
    return desc;
  }

  /**
   * @return the hidden
   */
  public boolean isHidden() {
    return hidden;
  }
}