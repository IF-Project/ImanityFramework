/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.impl;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.player.IPlayerBridge;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.store.StoreDatabase;

import java.util.Collection;
import java.util.UUID;

public class BukkitPlayerBridge implements IPlayerBridge<Player> {

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Imanity.PLUGIN.getServer().getOnlinePlayers();
    }

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }

    @Override
    public String getName(Player player) {
        return player.getName();
    }

    @Override
    public Class<Player> getPlayerClass() {
        return Player.class;
    }
}
