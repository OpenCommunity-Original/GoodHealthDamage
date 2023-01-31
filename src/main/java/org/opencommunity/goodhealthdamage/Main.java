package org.opencommunity.goodhealthdamage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
		String currentVersion = getDescription().getVersion();
		if (!config.contains("version") || !Objects.equals(config.getString("version"), currentVersion)) {
			config.set("version", currentVersion);
			generateConfig();
		}
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
		World world = Bukkit.getWorlds().stream().findFirst().orElse(null);
		if (world == null) {
			getLogger().warning("No world found, unable to generate config");
			return;
		}

		for (EntityType entityType : EntityType.values()) {
			if (entityType.isAlive() && !config.contains(entityType.name())) {
				Location loc = new Location(world, 0, 0, 0);
				try {
					assert entityType.getEntityClass() != null;
					Entity entity = loc.getWorld().spawn(loc, entityType.getEntityClass());
					System.out.println("entity " + entity);
					if (entity instanceof LivingEntity livingEntity) {
						System.out.println("livingEntity " + livingEntity);
						int health = (int) Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
						System.out.println("health " + health);
						AttributeInstance attackDamage = livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
						int damage = attackDamage != null ? (int) attackDamage.getValue() : 0;
						System.out.println("damage " + damage);
						config.set(entityType.name() + ".health", health);
						config.set(entityType.name() + ".damage", damage);
						entity.remove();
					}
				} catch (Exception e) {
					getLogger().warning("Error generating config for " + entityType + ": " + e.getMessage());
				}
			}
		}
		saveConfig();
	}
}