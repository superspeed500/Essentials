package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static com.earth2me.essentials.I18n.tlLiteral;

// The file commandsell.java has been used as a "template"
// Most of the code in this file is therefor a one-to-one copy from that file
public class Commandbuy extends EssentialsCommand {
    public Commandbuy() {
        super("buy");
    }

    // Starting point for when the command is run
    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        BigDecimal totalWorth = BigDecimal.ZERO;

        // Throw an error if the user has not specified enough arguments.
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        final ItemStack is = ess.getItemDb().get("gold_ingot", 1);
        int count = 0;

        totalWorth = totalWorth.add(buyItem(user, is, args));

        if (count != 1) {
            final AdventureUtil.ParsedPlaceholder totalWorthStr = AdventureUtil.parsed(NumberUtil.displayCurrency(totalWorth, ess));
            if (args[0].equalsIgnoreCase("blocks")) {
                user.sendTl("totalWorthBlocks", totalWorthStr, totalWorthStr);
            } else {
                user.sendTl("totalWorthAll", totalWorthStr, totalWorthStr);
            }
        }
    }

    private BigDecimal buyItem(final User user, final ItemStack is, final String[] args) throws Exception {
        final int amount = 1; // Replace this with actual amount
        final BigDecimal originalWorth = ess.getWorth().getPrice(ess, is);
        final BigDecimal worth = originalWorth == null ? null : originalWorth.multiply(ess.getSettings().getMultiplier(user));

        if (worth == null) {
            throw new TranslatableException("itemCannotBeBought");
        }

        if (amount <= 0) {
            return BigDecimal.ZERO;
        }

        final BigDecimal result = worth.multiply(BigDecimal.valueOf(amount));

        final ItemStack ris = is.clone();
        ris.setAmount(amount);
        
        Inventories.addItem(user.getBase(), user.isAuthorized("essentials.oversizedstacks") ? ess.getSettings().getOversizedStackSize() : 0, ris);

        user.getBase().updateInventory();
        Trade.log("Command", "Buy", "Item", user.getName(), new Trade(ris, ess), user.getName(), new Trade(result, ess), user.getLocation(), user.getMoney(), ess);
        // Needs to be changed to a method for taking money
        user.takeMoney(result, null, UserBalanceUpdateEvent.Cause.COMMAND_BUY);
        final String typeName = is.getType().toString().toLowerCase(Locale.ENGLISH);
        final AdventureUtil.ParsedPlaceholder worthDisplay = AdventureUtil.parsed(NumberUtil.displayCurrency(worth, ess));
        user.sendTl("itemBought", AdventureUtil.parsed(NumberUtil.displayCurrency(result, ess)), amount, typeName, worthDisplay);
        ess.getLogger().log(Level.INFO, ess.getAdventureFacet().miniToLegacy(tlLiteral("itemBoughtConsole", user.getName(), typeName, ess.getAdventureFacet().miniToLegacy(NumberUtil.displayCurrency(result, ess)), amount, ess.getAdventureFacet().miniToLegacy(worthDisplay.toString()), user.getDisplayName())));
        return result;
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            return getMatchingItems(args[0]);
        } else if (args.length == 2) {
            return Lists.newArrayList("1", "64");
        } else {
            return Collections.emptyList();
        }
    }
}
