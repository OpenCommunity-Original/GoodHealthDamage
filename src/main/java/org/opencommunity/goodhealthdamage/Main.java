package org.opencommunity.goodhealthdamage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin implements Listener {

	private FileConfiguration config;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();
		generateConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity livingEntity)) {
			return;
		}
		String entityType = entity.getType().name();
		int health = config.getInt(entityType + ".health", (int) Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
		int damage = config.getInt(entityType + ".damage", 0);
		Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(health);
		livingEntity.setHealth(health);
		Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(damage);
	}

	private void generateConfig() {
		for (EntityType entityType : EntityType.values()) {
			if (entityType.isAlive() && !config.contains(entityType.name())) {
				Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
				assert entityType.getEntityClass() != null;
				Entity entity = loc.getWorld().spawn(loc, entityType.getEntityClass());
				if (entity instanceof LivingEntity livingEntity) {
					int health = (int) Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
					int damage = (int) Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).getValue();
					config.set(entityType.name() + ".health", health);
					config.set(entityType.name() + ".damage", damage);
					entity.remove();
				}
			}
		}
		saveConfig();
	}
}