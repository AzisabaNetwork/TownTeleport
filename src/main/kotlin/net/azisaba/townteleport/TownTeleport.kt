package net.azisaba.townteleport

import net.azisaba.townteleport.config.ConfigFile
import net.azisaba.townteleport.config.DataConfig
import net.azisaba.townteleport.gui.AbstractScreen
import net.azisaba.townteleport.gui.PortalDeleteConfirmScreen
import net.azisaba.townteleport.gui.PortalScreen
import net.azisaba.townteleport.gui.PortalSettingsScreen
import net.azisaba.townteleport.gui.PortalTeleportPermissionScreen
import net.azisaba.townteleport.listener.PlayerListener
import net.azisaba.townteleport.listener.PortalListener
import net.azisaba.townteleport.task.ShowHologramTask
import net.azisaba.townteleport.util.ChannelUtil
import net.azisaba.townteleport.util.Holograms
import net.azisaba.townteleport.util.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TownTeleport : JavaPlugin() {
    lateinit var dataConfig: DataConfig

    override fun onEnable() {
        dataConfig = DataConfig(ConfigFile.load(File(dataFolder, "data.yml")))

        Bukkit.getPluginCommand("townteleport")?.let {
            it.setExecutor { sender, _, _, _ ->
                if (sender is Player) {
                    sender.inventory.addItem(ItemUtil.createPortalItem())
                }
                true
            }
        }

        Bukkit.getPluginManager().registerEvents(PortalListener(this), this)
        Bukkit.getPluginManager().registerEvents(PlayerListener, this)
        Bukkit.getPluginManager().registerEvents(AbstractScreen.EventListener, this)
        Bukkit.getPluginManager().registerEvents(PortalScreen.EventListener, this)
        Bukkit.getPluginManager().registerEvents(PortalDeleteConfirmScreen.EventListener, this)
        Bukkit.getPluginManager().registerEvents(PortalSettingsScreen.EventListener, this)
        Bukkit.getPluginManager().registerEvents(PortalTeleportPermissionScreen.EventListener, this)

        Bukkit.getOnlinePlayers().forEach { player ->
            ChannelUtil.inject(player)
        }

        ShowHologramTask(this).runTaskTimer(this, 60, 60)
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player ->
            ChannelUtil.eject(player)
            Holograms.hideAll(player)
        }

        save()
    }

    private fun save() {
        dataConfig.save()
        ConfigFile.save(File(dataFolder, "data.yml"), dataConfig.config)
    }

    fun saveAsync() {
        dataConfig.save()
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            ConfigFile.save(File(dataFolder, "data.yml"), dataConfig.config)
        })
    }
}
