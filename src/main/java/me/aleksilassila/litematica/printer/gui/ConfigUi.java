package me.aleksilassila.litematica.printer.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.config.Configs;
import dev.bilixwhite.litematica.printer.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConfigUi extends GuiConfigsBase {
    private static Tab tab = Tab.CORE;

    public ConfigUi(@Nullable Screen parent) {
        super(10, 50, Reference.MOD_ID, parent, Reference.MOD_NAME + " " + ModUtils.LOCAL_VERSION + "   " + I18n.FREE_NOTICE.getName().getString());
    }

    public ConfigUi() {
        this(Minecraft.getInstance().screen);
    }

    public static void refresh() {
        if (Reference.MINECRAFT.screen instanceof ConfigUi gui) {
            gui.initGui();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();
        int x = 10;
        int y = 26;
        for (Tab tab : Tab.values()) {
            x += this.createButton(x, y, -1, tab);
        }
    }

    public void reset() {
        reCreateListWidget();
        Objects.requireNonNull(getListWidget()).resetScrollbarPosition();
        initGui();
    }

    private int createButton(int x, int y, int width, Tab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getName(), tab.getComment());
        button.setEnabled(ConfigUi.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));
        return button.getWidth() + 2;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<? extends IConfigBase> configs;
        Tab tab = ConfigUi.tab;

        if (tab == Tab.ALL) {
            return this.getAllConfigs();
        } else if (tab == Tab.CORE) {
            configs = Configs.Core.OPTIONS;
        } else if (tab == Tab.PLACEMENT) {
            configs = Configs.Placement.OPTIONS;
        } else if (tab == Tab.BREAK) {
            configs = Configs.Break.OPTIONS;
        } else if (tab == Tab.PRINT) {
            configs = Configs.Print.OPTIONS;
        } else if (tab == Tab.EXCAVATE) {
            configs = Configs.Mine.OPTIONS;
        } else if (tab == Tab.FILL) {
            configs = Configs.Fill.OPTIONS;
        } else if (tab == Tab.FLUID) {
            configs = Configs.Fluid.OPTIONS;
        } else if (tab == Tab.HOTKEYS) {
            configs = Configs.Hotkeys.OPTIONS;
        } else {
            return Collections.emptyList();
        }
        return ConfigOptionWrapper.createFor(configs);
    }

    public List<ConfigOptionWrapper> getAllConfigs() {
        List<ConfigOptionWrapper> configs = new ArrayList<>();

        configs.addAll(ConfigOptionWrapper.createFor(Configs.Core.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Placement.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Break.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Print.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Mine.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Fill.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Fluid.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Hotkeys.OPTIONS));

        return configs;
    }

    public enum Tab {
        ALL(I18n.of("category.all")),
        CORE(I18n.of("category.core")),
        PLACEMENT(I18n.of("category.placement")),
        BREAK(I18n.of("category.break")),
        HOTKEYS(I18n.of("category.hotkeys")),
        PRINT(I18n.of("category.print")),
        EXCAVATE(I18n.of("category.mine")),
        FILL(I18n.of("category.fill")),
        FLUID(I18n.of("category.fluid"));

        private final I18n i18n;

        Tab(I18n i18n) {
            this.i18n = i18n;
        }

        public String getName() {
            return i18n.getConfigName().getString();
        }

        public String getComment() {
            return i18n.getConfigDesc().getString();
        }
    }

    public record ButtonListener(Tab tab, ConfigUi parent) implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            ConfigUi.tab = this.tab;
            this.parent.reset();
        }
    }
}